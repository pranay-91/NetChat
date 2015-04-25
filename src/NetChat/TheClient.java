/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetChat;


import java.awt.Color;
import java.io.IOException;
import java.net.ConnectException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.util.Iterator;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.table.DefaultTableModel;
import javax.swing.text.BadLocationException;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

/**
 *
 * @author Pronoy
 */
public class TheClient extends javax.swing.JFrame implements Runnable {
private static final int BUFFER_SIZE = 255;
    private static final long CHANNEL_WRITE_SLEEP = 10L;
    private static final int PORT = 10997;
  
    private DefaultTableModel Model ;
    private ByteBuffer writeBuffer;
    private ByteBuffer readBuffer;
    private boolean running;
    private SocketChannel channel;
    private String host;
    private Selector readSelector;
    private CharsetDecoder asciiDecoder;

    private String user;
    private boolean user_exists=false;
    private int Random;
    private boolean RandomCheck=true;
  
public void runGUI()
{
              //<editor-fold defaultstate="collapsed" desc=" Look and feel setting code (optional) ">
        /*
         * If Nimbus (introduced in Java SE 6) is not available, stay with the
         * default look and feel. For details see
         * http://download.oracle.com/javase/tutorial/uiswing/lookandfeel/plaf.html
         */
        try {
            for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
                if ("Nimbus".equals(info.getName())) {
                    javax.swing.UIManager.setLookAndFeel(info.getClassName());
                    break;
                }
            }
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | javax.swing.UnsupportedLookAndFeelException ex) {
            java.util.logging.Logger.getLogger(TheClient.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
        //</editor-fold>
initComponents();
jTextPane1.setBackground(Color.ORANGE);
}

    /**
     * Creates new form TheClient
     * @param host 
     * @param user 
     */
    public TheClient(String host, String user) {
        
        this.host = host;
        this.user=user;
	writeBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	readBuffer = ByteBuffer.allocateDirect(BUFFER_SIZE);
	asciiDecoder = Charset.forName( "US-ASCII").newDecoder();;
        initComponents();
    }

  
    
    public void setUser(String usr)
    {
        this.user=usr;
        sendMessage("log "+usr);
        System.out.print(usr);
    }
    

   
    public boolean getConnectionState()
    {
        return user_exists;
    }
 
    public void run(){
       
	connect(host);
	running = true;
        readInput("log "+user);
    
 //   Model.addRow(new Object[]{user});
         
    writeTextBox("Server: ","Welcome "+ user);
    
      
	while (running) {
	    readIncomingMessages();
         // readInput();
	    // nap for a bit
	    try {
		Thread.sleep(50);
	    }
	    catch (InterruptedException ie) {
	    }
	}
    }

    private void connect(String hostname) {
	try {
	    readSelector = Selector.open();
	    InetAddress addr = InetAddress.getByName(hostname);
	                 
            channel = SocketChannel.open(new InetSocketAddress(addr, PORT));
	    channel.configureBlocking(false);
	    channel.register(readSelector, SelectionKey.OP_READ, new StringBuffer());
	}
	catch (UnknownHostException uhe) {
	}
	catch (ConnectException ce) {
	}
	catch (Exception e) {
	}
    }
    private void writeTextBox(String label, String msg)
    {
       try
      { 
        msg =msg +"\n";
        StyledDocument doc = jTextPane1.getStyledDocument();
        SimpleAttributeSet keyWord = new SimpleAttributeSet();
                     
        StyleConstants.setForeground(keyWord, Color.red);
        StyleConstants.setBackground(keyWord, Color.white);
        StyleConstants.setBold(keyWord, true);
        doc.insertString(doc.getLength()," " + label  + "\t", keyWord);                  
                        
        StyleConstants.setForeground(keyWord, Color.black);
        StyleConstants.setBackground(keyWord, Color.white);
        StyleConstants.setBold(keyWord, false);
        doc.insertString(doc.getLength(), msg, keyWord);
     }catch(BadLocationException be){}
                     
    }
    
    /**
     *
     */
    public void readIncomingMessages() {
	// check for incoming mesgs
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
		readBuffer.clear();
		
		// read from the channel into our buffer
		long nbytes = channel.read(readBuffer);

		// check for end-of-stream
		if (nbytes == -1) { 
		    System.out.println("disconnected from server: end-of-stream");
		    channel.close();
		    shutdown();
		   // it.Inputshutdown();
		}
		else {
		    // grab the StringBuffer we stored as the attachment
		    StringBuffer sb = (StringBuffer)key.attachment();

		    // use a CharsetDecoder to turn those bytes into a string
		    // and append to our StringBuffer
		    readBuffer.flip( );
		    String str = asciiDecoder.decode( readBuffer).toString( );
		    sb.append( str );
		    readBuffer.clear( );

		    // check for a full line and write to STDOUT
		    String line = sb.toString();
		    if ((line.indexOf("\n") != -1) || (line.indexOf("\r") != -1)) {
			sb.delete(0,sb.length());
                        
             
                      
                          //if(line.startsWith("Online")) {
                            // writeTextBox("Server:",line);
                           // }
                          
                          
                           if (line.startsWith("log"))
                          {
                              user_exists=true;
                                writeTextBox("Server","User exists " +user_exists );
                       
                        
                     
                           }
                          else if(line.startsWith("@GAMEON"))
                          {
                           
                              
                              System.out.println("Game String : " + line);  
          
                              char ch = line.charAt(8);
                        
                              int user_size=Character.getNumericValue(ch);
                              
                              
                              int cutoff = user_size+9;
                                      
                              System.out.println("user size:" + user_size);
                              System.out.println("cutoff size: " + cutoff);     
                               
                                        
                              String user_name = line.substring(10,cutoff);
                              cutoff+=2;
                              String Rand= line.substring(cutoff).trim();
                              Random=Integer.parseInt(Rand);
                              RandomCheck =true;
                              System.out.println("user name: " + user_name);     
                              System.out.println("Random numnber: " + Random);  
                              
                              writeTextBox("Server:"," Game initiated by " + user_name);
                              writeTextBox("Server:","Please Guess the Random number ??");
                              writeTextBox("Server:", "Type @QUIT to give up");
                                        
                              System.out.println("Got Random number: " + Random);
                              System.out.println("Got Random number2: " + line.substring(cutoff));
                                                
                                                
                              
                          
                            
                              
                               
                                
                          }
                          
                          else if (line.startsWith("@FRAND"))
                          {
                          
                                String Rand= line.substring(7).trim();
                                Random =Integer.parseInt(Rand);
                                RandomCheck=true;
                                
                                writeTextBox("Server:"," Guess the Random number of the Server ");
                                System.out.println(host);
                                System.out.println("Got Random number: " + Random);
                                System.out.println("Got Random number2: " + line.substring(7));
                          }
                        else {
                            writeTextBox("Client:",line);
                        }
                     }
		}
	    }		
	}
	catch (IOException ioe) {
	}

    }
    
    private void sendMessage(String mesg) {
	prepWriteBuffer(mesg);
	channelWrite(channel, writeBuffer);
      
        
    }
    
    

    private void prepWriteBuffer(String mesg) {
	// fills the buffer from the given string
	// and prepares it for a channel write
	writeBuffer.clear();
	writeBuffer.put(mesg.getBytes());
	writeBuffer.flip();
    }
    
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
	writeBuffer.rewind();
    }
    /**
     *
     * @param s
     */
    public void readInput(String s)
    {
                        
                        
		    if (s.length() > 0)
                    {
                        sendMessage(s + "\n");
			if(s.equals("quit"))
                       {
                     
                            try {
                            Thread.sleep(50L);
                                }
                            catch (InterruptedException ie) {  }
                             running = false;
              //    this.shutdown();
                        }
                    }
          
    }

    /**
     *
     */
    public void shutdown() {
	running = false;
	//interrupt();
    }
    
 
    /**
     * This method is called from within the constructor to initialize the form.
     * WARNING: Do NOT modify this code. The content of this method is always
     * regenerated by the Form Editor.
     */
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        jDialog1 = new javax.swing.JDialog();
        jTextField1 = new javax.swing.JTextField();
        jButton1 = new javax.swing.JButton();
        jButton2 = new javax.swing.JButton();
        jScrollPane3 = new javax.swing.JScrollPane();
        jTextPane1 = new javax.swing.JTextPane();

        javax.swing.GroupLayout jDialog1Layout = new javax.swing.GroupLayout(jDialog1.getContentPane());
        jDialog1.getContentPane().setLayout(jDialog1Layout);
        jDialog1Layout.setHorizontalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 400, Short.MAX_VALUE)
        );
        jDialog1Layout.setVerticalGroup(
            jDialog1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 300, Short.MAX_VALUE)
        );

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setBackground(new java.awt.Color(255, 255, 102));
        addWindowListener(new java.awt.event.WindowAdapter() {
            public void windowClosing(java.awt.event.WindowEvent evt) {
                formWindowClosing(evt);
            }
            public void windowClosed(java.awt.event.WindowEvent evt) {
                formWindowClosed(evt);
            }
        });

        jTextField1.setText("Type your message here..");
        jTextField1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jTextField1.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                jTextField1MouseClicked(evt);
            }
        });
        jTextField1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jTextField1ActionPerformed(evt);
            }
        });

        jButton1.setBackground(new java.awt.Color(255, 255, 255));
        jButton1.setText("Send");
        jButton1.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton1ActionPerformed(evt);
            }
        });

        jButton2.setBackground(new java.awt.Color(255, 255, 255));
        jButton2.setText("Close");
        jButton2.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                jButton2ActionPerformed(evt);
            }
        });

        jTextPane1.setBorder(javax.swing.BorderFactory.createEmptyBorder(1, 1, 1, 1));
        jScrollPane3.setViewportView(jTextPane1);

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                    .addGroup(layout.createSequentialGroup()
                        .addComponent(jButton1)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                        .addComponent(jButton2))
                    .addComponent(jScrollPane3, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addComponent(jTextField1, javax.swing.GroupLayout.Alignment.LEADING, javax.swing.GroupLayout.PREFERRED_SIZE, 405, javax.swing.GroupLayout.PREFERRED_SIZE))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(jScrollPane3, javax.swing.GroupLayout.DEFAULT_SIZE, 349, Short.MAX_VALUE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jTextField1, javax.swing.GroupLayout.PREFERRED_SIZE, 23, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                    .addComponent(jButton2)
                    .addComponent(jButton1))
                .addGap(18, 18, 18))
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    
    
    private void jButton2ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton2ActionPerformed
    
    readInput("quit");

    this.setVisible(false);
    this.dispose();
    
        // TODO add your handling code here:
    }//GEN-LAST:event_jButton2ActionPerformed

    
    
    private void jButton1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jButton1ActionPerformed

        
        String text = jTextField1.getText().toUpperCase();
        writeTextBox("Me:",jTextField1.getText());
        int random =0; 
       
         if(RandomCheck == true)
         {
              if(text.startsWith("@QUIT"))
              {
                   writeTextBox("Server:","Random number is: " + Random);
                   RandomCheck =false;
               }
         
               else
               {
                  try
                  {
                      random=Integer.parseInt(text);
                
                      if(random >Random  ) 
                      {
                            writeTextBox("Server:","Random number is smaller than that..");
                      } 
                      else if(random<Random) 
                      {
                           writeTextBox("Server:","Random number is greater than that..");
                       }
                      else 
                        {
                            writeTextBox("Server:","CORRECTAMUNDO");
                            RandomCheck= false;
                        }
                   }
                  catch(Exception E)
                   {
                       writeTextBox("Server","Please Guess the random number first..");
                       writeTextBox("Server","Type @quit to give up random number ..");
                   }
                  }   
          }
     
     
         else if(RandomCheck == false)
         {
        
             if(text.startsWith("@GAMEON"))
             {
                 try
                 {
                    random =Integer.parseInt(text.substring(8).trim());
                    if(random>49 && random<61) 
                     {
                        readInput("@GAMEON " + user.trim().length() + "-" + user.trim() + "-" + random);
                        System.out.println("Random number is : "+ random);
                        writeTextBox("Server:"," Random number set to: "+ random);
                        
                    }
                    else
                    {
                         writeTextBox("Server:","Random number must be between 50 and 60.");
                         writeTextBox("Server:","Please Start again ");
                    }
                   
                 }
                  
                  catch(Exception Ex)
                  {
                        
                       writeTextBox("Server","Please Enter a valid number ..");
                       writeTextBox("Server:","Please Start again ");
                  }                  
            }
             else if (text.startsWith("@"))
             {
                  writeTextBox("Server:","SYNTAX ERROR");
             }
             else
             {
                  readInput(jTextField1.getText());
             }
      }
     
     
// TODO add your handling code here:
    }//GEN-LAST:event_jButton1ActionPerformed

    private void formWindowClosed(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosed
        // TODO add your handling code here:
       
    }//GEN-LAST:event_formWindowClosed

    private void formWindowClosing(java.awt.event.WindowEvent evt) {//GEN-FIRST:event_formWindowClosing
        // TODO add your handling code here:
        
         sendMessage("closing "+ user);
         try {
            Thread.sleep(250L);
        } catch (InterruptedException ex) {
            Logger.getLogger(JLogin.class.getName()).log(Level.SEVERE, null, ex);
        }
         System.out.println("closing on form closing");
         
    }//GEN-LAST:event_formWindowClosing

    private void jTextField1ActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_jTextField1ActionPerformed
        // TODO add your handling code here:
    }//GEN-LAST:event_jTextField1ActionPerformed

    private void jTextField1MouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_jTextField1MouseClicked
        // TODO add your handling code here:
        jTextField1.setText("");
        
    }//GEN-LAST:event_jTextField1MouseClicked

    /**
     * @param args the command line arguments
     */
  
    
    
    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton jButton1;
    private javax.swing.JButton jButton2;
    private javax.swing.JDialog jDialog1;
    private javax.swing.JScrollPane jScrollPane3;
    private javax.swing.JTextField jTextField1;
    private javax.swing.JTextPane jTextPane1;
    // End of variables declaration//GEN-END:variables
}
