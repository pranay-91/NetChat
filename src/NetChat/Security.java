/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package NetChat;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;

/**
 *
 * @author Pronoy
 */
public class Security {
    
    public boolean checkPass(String pass)
    {
    String pwd="*";
    pass=pass.trim();
    
       
    //System.out.println("Given: " + pass);
    
     int count;
       
     
         char ch;
  
      
         
         try(FileChannel fChan = (FileChannel)Files.newByteChannel(Paths.get("dist/Demo.txt"), StandardOpenOption.READ))
    {
        ByteBuffer mBuf = ByteBuffer.allocate(48);
      
        do
        {
             count = fChan.read(mBuf);
                 if(count !=1)
                {
                    mBuf.rewind();
                    int i=0;
                    while(i < count)
                    {
                       ch=(char)mBuf.get();
                      //  System.out.print(ch);
                        
                        if(ch=='\n')
                      {
                             // System.out.println("Users  " + pwd.trim());
                              pwd=pwd.trim();
                              
                            if(pwd.equals(pass)){
                              
                               
                                return true;
                            }
                            pwd="*";
                        }
                        else {
                            pwd= pwd.concat(Character.toString(ch));
                        }
                      
                        ++i;
                    }
                }
            
        }while(count !=-1);
                
      
        
    }
    catch(InvalidPathException e)
    {
        System.out.print("Path Doesnt Exist: "+ e);
    }
    
          catch(IOException ex){}
    
      return false;
    }
  
 
}