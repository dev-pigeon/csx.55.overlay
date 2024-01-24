package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import transport.TCPReceiverThread;
import transport.TCPSender;
import transport.TCPServerThread;

public class RegisteredNode {
    
   public Socket socket;
   public String ipAddress;
   public int portNumber;
   //need a field to denote if its owned by msg or registry
   private Object owner;

   
   

   /* Note
    * Registered nodes can be two things
    1.) they can be literal registered nodes in the registry
    2.) or they can be the objects containing the sockets for the other nodes
         that a given messaging node is connected to, 
    */

    

   public RegisteredNode(Socket socket, Object owner, int port) throws IOException {
      this.socket = socket;
      this.ipAddress = socket.getInetAddress().getHostAddress();
      this.portNumber = port;
      System.out.println("CREATED REGISTERED NODE WITH PORT = " + portNumber);
    //same list maintained by a given MSG node or the registry (depends on owner)
      this.owner = owner;
      setUpandRun(); //these need to listen and be able to send, but also know the size of array

   }

   public void setUpandRun() throws IOException {
      //create the receiver thread


      //about to start the receiver thread for the connected pre registered node

      //need to create a server socket to use the TCPServerthread for connections with other people!
      /* 
      iniServerSocket();
      TCPServerThread server = new TCPServerThread(serverSocket, test, owner);
      Thread sThread = new Thread(server);
      sThread.start();
      */

      TCPReceiverThread receiver = new TCPReceiverThread(socket, owner);
      Thread receiverThread = new Thread(receiver);
      receiverThread.start();
   }

}
