package node;

import java.io.IOException;
import java.net.Socket;

import transport.TCPReceiverThread;

public class RegisteredNode {
    
   public Socket socket;
   public String ipAddress;
   int portNum;
   
   /* updated note
      registered nodes are nothing more than the Object representation of a messaging node that is connected to 
      the registry, but they need their own receivers so that the sister node (in msgNode) can send their request
      and so these guys can send their response
    */

    //you may find yourself wondering how we deal with the R response in the msg node since we dont have an owner
    //the asnwer is we don't we just need to print, and since that obj is on the msg node machine, it will print
   public RegisteredNode(Socket socket) throws IOException {
      this.socket = socket;
      this.ipAddress = socket.getInetAddress().getHostAddress();
      setUpandRun(); 
   }

   public void setUpandRun() throws IOException {
      //create the receiver thread
      TCPReceiverThread receiver = new TCPReceiverThread(socket, this);
      Thread receiverThread = new Thread(receiver);
      receiverThread.start();
   }

   //this is needed because when the Registry's R node gets the request we have to have their ServerSockets port num set 
   //for when registry sends the Messaging list or whatever
   public void setPortNum(int portNum) {
      this.portNum = portNum;
   }
}
