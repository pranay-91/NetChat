/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetChat;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.*;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import javax.swing.table.DefaultTableModel;

/**
 *
 * @author Pronoy
 */
public class NewJFrame extends javax.swing.JFrame implements Runnable{
    

    private DefaultTableModel Model ;
    private DefaultTableModel Model_Users;
    private static final int BUFFER_SIZE = 255;
    private static final long CHANNEL_WRITE_SLEEP = 10L;
    private static final int PORT = 10997;
    
    private ServerSocketChannel serverSocketChannel;
 
    private Selector acceptSelector;
    private Selector readSelector;
    private SelectionKey selectKey;
    private ByteBuffer readBuff;
    private ByteBuffer writeBuff;
    
    private boolean isServerRunning;
    private LinkedList clients;
    private LinkedList<String> users;
    private CharsetDecoder asciiDecoder;
    private InetAddress addr;
   
    
    // Writes the data on the channel 
    private void channelWrite(SocketChannel channel, ByteBuffer writeBuffer) {
	long nbytes = 0;
	long toWrite = writeBuffer.remaining();

	// loop on the channel.write() call since it will not necessarily
	// write all bytes in one shot
	try {
	    while (nbytes != toWrite) {
		nbytes += channel.write(writeBuffer);
		
		try {
		    Thread.sleep(CHANNEL_WRITE_SLEEP);
		}
		catch (InterruptedException e) {}
	    }
	}
	catch (ClosedChannelException cce) {
	}
	catch (Exception e) {
	} 
	
	// get ready for another write if needed
	writeBuff.rewind();
    }
    
    // Uploads the data in the buffer
    private void prepWriteBuffer(String mesg) {
	// fills the buffer from the given string
	// and prepares it for a channel write
	writeBuff.clear();
	writeBuff.put(mesg.getBytes());
	writeBuff.putChar('\n');
	writeBuff.flip();
    }
    
    // Send message to specific channel
     private void sendMessage(SocketChannel channel, String mesg) {
	prepWriteBuffer(mesg);
	channelWrite(channel, writeBuff);
    }
    
     
     // Send broadcast message to all the channels
    private void sendBroadcastMessage(String mesg, SocketChannel from) {
	prepWriteBuffer(mesg);
	Iterator i = clients.iterator();
	while (i.hasNext()) {
	    SocketChannel channel = (SocketChannel)i.next();
	    if (channel != from) {
                channelWrite(channel, writeBuff);
            }
	}
    }
    
    // initialise the server socke by enabling the non blocking mode
    public void initialiseServerSocket() 
    {
        try
        {
            //open a non blocking server socket channel
            
            serverSocketChannel = ServerSocketChannel.open();
            serverSocketChannel.configureBlocking(false);
            
            //bind to localhost on designated port
            addr = InetAddress.getLocalHost();
            serverSocketChannel.socket().bind(new InetSocketAddress(addr, PORT));
            
            //get a selector for multiplexing client channels
            readSelector= Selector.open();
            
            //System.out.println("Server: " + addr.getHostAddress().toString());
            jTextField1.setText(addr.getHostAddress().toString()); 
         
  
     
            Model.addRow(new Object[]{"Server Listening at",addr.getHostAddress().toString()});
         
        
        }catch(Exception e){
        
        }
     }
    
    
    //Registers the socket channel to the selection keys
    private void addNewClient(SocketChannel chan) throws IOException
    {
        
        
        try
        {
            chan.configureBlocking(false);
         chan.register(readSelector, SelectionKey.OP_READ, new StringBuffer());
            
        }catch(ClosedChannelException cce){}
        
        catch(IOException ioe){}
    }
    
    
    
    // Accept incomming connections  from the clients and register the channels
   public void acceptIncommingConnection()
    {
        try
        {
            SocketChannel clientChannel;
            while((clientChannel = serverSocketChannel.accept()) != null)
            {
                addNewClient(clientChannel);
               
                jTextField2.setText(clientChannel.socket().getInetAddress().getHostAddress().toString());
               
              Model.addRow(new Object[] {"Got Connection From", clientChannel.socket().getInetAddress().getHostAddress().toString()});
            //sendMessage(clientChannel,"Server Welcome");
                
            }
        }
        catch(IOException ioe){}
        
        catch (Exception e){}
        
    }
   
   
   //check whether there is multiple user of same type is online
   private boolean handleUser(String user)
   {
       Iterator i = users.iterator();
       String currentUser=null;
       
      System.out.println("==============");
	while (i.hasNext()) {
	   
	    currentUser =(String)i.next();
            System.out.println("Stack user: "+currentUser);
            if (currentUser.equals(user)) 
            {	
                System.out.println("Inside if exit...");
               
                return true;
            }   
            
	}
        
        
            System.out.println("New user: "+user);
            
            System.out.println("---------------------");
            System.out.println("user: "+ users.size());
            
            System.out.println("client: "+clients.size());
        return false;
        
   }
   
   //generates a random number whenever a new client connects
   private int generateRandom()
   {
       double  Rand = (50 + Math.random() * 10);
       
       return (int)Rand;
       
   }
   
   //reads incomming messages from the client and filters it accordingly
    private void readIncomingMessages() {
	try {
	    // non-blocking select, returns immediately regardless of how many keys are ready
	    readSelector.selectNow();
	    
	    // fetch the keys
	    Set readyKeys = readSelector.selectedKeys();
	    
	    // run through the keys and process
	    Iterator i = readyKeys.iterator();
	    while (i.hasNext()) {
		SelectionKey key = (SelectionKey) i.next();
		i.remove();
		SocketChannel channel = (SocketChannel) key.channel();
		readBuff.clear();
		
		// read from the channel into our buffer
		long nbytes = channel.read(readBuff);
		
		// check for end-of-stream
		if (nbytes == -1) { 
		  //  log.info("disconnect: " + channel.socket().getInetAddress() + ", end-of-stream");
                     Model.addRow(new Object[]{"End-of-stream, Disconnected from : " ,channel.socket().getInetAddress()});
                   System.out.println("disconnect: " + channel.socket().getInetAddress() + ", end-of-stream");
		    
		    sendBroadcastMessage(users.get(clients.indexOf(channel)) + "is offline" , channel);
                    users.remove(clients.indexOf(channel));
                     clients.remove(channel);
		    channel.close();
                    
                }
		else {
		    // grab the StringBuffer we stored as the attachment
		 //   StringBuffer sb = (StringBuffer)key.attachment();
		    
		    // use a CharsetDecoder to turn those bytes into a string
		    // and append to our StringBuffer
		    readBuff.flip( );
		    String str = asciiDecoder.decode( readBuff).toString( );
		    readBuff.clear( );
		    //sb.append( str);
		    
		    // check for a full line
		   
                    String line = str.toString();
                    
                    //String line = sb.toString();
		    if ((line.indexOf("\n") != -1) || (line.indexOf("\r") != -1)) {
			line = line.trim();
			if (line.startsWith("quit")) {
			    // client is quitting, close their channel, remove them from the list and notify all other clients
                            // log.info("got quit msg, closing channel for : " + channel.socket().getInetAddress());
                             Model.addRow(new Object[]{"Got quit msg, closing channel for : " ,channel.socket().getInetAddress()});
                          //  System.out.println("got quit msg, closing channel for : " + channel.socket().getInetAddress());
			    sendBroadcastMessage(users.get(clients.indexOf(channel)) + "is offline" , channel);
                            users.remove(clients.indexOf(channel));
                             clients.remove(channel);
                        	    channel.close();
                    
			}
                                                 
                        else if(line.startsWith("log "))  // gets the ogin info to identify the user login info
                            {       
                                String user = line.substring(4);    
                                jTable2.setModel(Model_Users);
                                             System.out.println("new user");
                                if(clients.isEmpty() )
                                {
                                   System.out.println("new user");
                                   clients.add(channel);
                                   users.add(user);
                                   sendMessage(channel,"@FRAND " + String.valueOf(generateRandom()));
                                   Model_Users.addRow(new Object[]{user});                                
                                }
                                else 
                                {       if( handleUser(user)) 
                                        {          
                                            sendMessage(channel,"log exists");
                                            Model.addRow(new Object[]{"broadcasting log: " , channel.getLocalAddress()});
                                        }
                                        else {
                                            clients.add(channel);
                                            users.add(user);
                                            Model_Users.addRow(new Object[]{user});
                                            System.out.println(generateRandom());
                                            
                                            sendMessage(channel,"@FRAND"+ String.valueOf(generateRandom()));
                                            sendBroadcastMessage(user + " is online",channel);
                                            }
                                }
                            } 
                     
                        else if (line.startsWith("@GAMEON"))  // Broadcasts the fame info to all the clients connected
                        {
                            sendBroadcastMessage(line,channel);
                            
                        }
                        else
                             {             
                             Model.addRow(new Object[]{"broadcasting: " , line});
                             sendBroadcastMessage(line, channel);
                        
                      //      sb.delete(0,sb.length());
                             }
                            
                            
			
		    }
		}
		
	    }		
	}
	catch (IOException ioe) {
	  //  log.warn("error during select(): ", ioe);
	}
	catch (Exception e) {
	 //   log.error("exception in run()", e);
	}
	
    }
    
    public void run()
    {
        initialiseServerSocket();
        isServerRunning=true;
     
      while(isServerRunning)
        {
            acceptIncommingConnection();
            
             readIncomingMessages();
          try{
             Thread.sleep(100);
           }catch(InterruptedException ie){}
        }
    
        
    }
    /**
     * Creates new form NewJFrame
     */
    public NewJFrame() {
      
        Model = new DefaultTableModel(null,new Object[] {"Events","Description"});
       Model_Users= new DefaultTableModel(null, new Object[]{"Users Online"});
        
        
        initComponents();
        
 
 // Link list for 
        clients = new LinkedList();     // new client list for adding new clients
       users = new LinkedList();
       
        
        readBuff = ByteBuffer.allocate(BUFFER_SIZE);
        writeBuff = ByteBuffer.allocate(BUFFER_SIZE);
        asciiDecoder = Charset.forName("US-ASCII").newDecoder();
        
    }
  

    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jTextField1 = new javax.swing.JTextField();
        jTextField2 = new javax.swing.JTextField();
        jLabel1 = new javax.swing.JLabel();
        jLabel2 = new javax.swing.JLabel();
        jScrollPane1 = new javax.swing.JScrollPane();
        jTable1 = new javax.swing.JTable();
        jScrollPane2 = new javax.swing.JScrollPane();
        jTable2 = new javax.swing.JTable();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 204, 51));
        setForeground(java.awt.Color.white);

        jButton1.setBackground(new java.awt.Color(255, 153, 51));
        jButton1.setText("Connect");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 153, 0));
        jButton2.setText("Terminate");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jLabel1.setText("Server Address");

        jLabel2.setText("Connected To");

        jTable1.setAutoCreateRowSorter(true);
        jTable1.setBackground(new java.awt.Color(255, 204, 102));
        jTable1.setModel(Model);
        jScrollPane1.setViewportView(jTable1);

        jTable2.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {
                {null}
            },
            new String [] {
                "Users Online"
            }
        ));
        jScrollPane2.setViewportView(jTable2);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(44, 44, 44)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jLabel1)
                            .addComponent(jLabel2))
                        .addGap(30, 30, 30)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, 207, javax.swing.GroupLayout.PREFERRED_SIZE)))
                    .addGroup(javax.swing.GroupLayout.Alignment.LEADING, layout.createSequentialGroup()
                        .addGap(172, 172, 172)
                        .addComponent(jButton1)
                        .addGap(18, 18, 18)
                        .addComponent(jButton2))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 345, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(18, 18, 18)
                .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 157, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(22, 22, 22)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel1))
                        .addGap(25, 25, 25)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                            .addComponent(jLabel2)
                            .addComponent(jTextField2, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(34, 34, 34)
                        .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 164, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(18, 18, 18)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(jButton1)
                            .addComponent(jButton2)))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(jScrollPane2, javax.swing.GroupLayout.PREFERRED_SIZE, 320, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed
        // TODO add your handling code here:
      
     //  new Thread(jfr).start();
        
      new Thread(this).start();
   
     
     
    }//GEN-LAST:event_jButton1ActionPerformed

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
        // TODO add your handling code here:
        this.setVisible(false);
        this.dispose();
        
        
    }//GEN-LAST:event_jButton2ActionPerformed

    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JLabel jLabel1;
    private javax.swing.JLabel jLabel2;
    private javax.swing.JScrollPane jScrollPane1;
    private javax.swing.JScrollPane jScrollPane2;
    private javax.swing.JTable jTable1;
    private javax.swing.JTable jTable2;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextField jTextField2;
    // End of variables declaration//GEN-END:variables


}
