package node;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import transport.TCPReceiverThread;

public class RegisteredNode {
    
   public Socket socket;
   public String ip;
   public int portNum;
   Object owner;

   public HashMap<RegisteredNode,Integer> peerNodes = new HashMap<>();

   public Set<RegisteredNode> conectionList = new HashSet<>();

   public ArrayList<String> connectionMessageList = new ArrayList<>();

   
   public RegisteredNode(Object owner, String ip, int port) {
      this(null, owner, ip, port);
   }


   public RegisteredNode(Socket socket, Object owner, String ip, int port) {
      this.socket = socket;
      this.owner = owner;
      this.ip = ip;
      this.portNum = port;
   }

   public void setUpandRun() throws IOException {
      //create the receiver thread
      if(socket!=null) {
         TCPReceiverThread receiver = new TCPReceiverThread(socket, owner, this);
         Thread receiverThread = new Thread(receiver);
         receiverThread.start();
      } 
      
   }

   //this is needed because when the Registry's R node gets the request we have to have their ServerSockets port num set 
   //for when registry sends the Messaging list or whatever
   public void setPortNum(int portNumber) {
      System.out.println("my port num is now " + portNumber);
      this.portNum = portNumber;
   }

   public void addMemberToConnectionList(RegisteredNode toRemove) {   
      conectionList.add(toRemove);
   }
}
