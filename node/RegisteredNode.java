package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.util.ArrayList;

import transport.TCPReceiverThread;
import transport.TCPSender;

public class RegisteredNode {
    
   private static Socket socket;
   public String ipAddress;
   public int portNumber;
   ArrayList<RegisteredNode> registeredNodes = new ArrayList<>();
   //need a field to denote if its owned by msg or registry
   private String owner;
   

   /* Note
    * Registered nodes can be two things
    1.) they can be literal registered nodes in the registry
    2.) or they can be the objects containing the sockets for the other nodes
         that a given messaging node is connected to, 
    */


   public RegisteredNode(Socket socket, ArrayList<RegisteredNode> registeredNodes, String owner) throws IOException {
      this.socket = socket;
      this.ipAddress = socket.getInetAddress().getHostAddress();
      this.portNumber = socket.getPort();
      this.registeredNodes = registeredNodes; //same list maintained by a given MSG node or the registry (depends on owner)
      this.owner = owner;
      setUpandRun(); //these need to listen and be able to send, but also know the size of array

   }

   public void setUpandRun() throws IOException {
      //create the receiver thread


      //about to start the receiver thread for the connected pre registered node

      TCPReceiverThread receiver = new TCPReceiverThread(socket, registeredNodes, owner);
      Thread receiverThread = new Thread(receiver);
      receiverThread.start();
      //need to figure out best way to register

      
   }
}
