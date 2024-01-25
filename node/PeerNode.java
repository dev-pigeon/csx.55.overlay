package node;

import java.io.IOException;
import java.net.Socket;
import transport.*;
public class PeerNode {
    
    Socket socket;
    String ip;
    MessagingNode owner;
    int port;


    //we do not need a owner for the same reason that we do not 
    //need one in the registered node
    //might actually need cause dont wanna make those methods public static, so lets just do that
    public PeerNode(Socket socket, MessagingNode owner, int port) throws InterruptedException {
       
        this.socket = socket;
        this.ip = socket.getInetAddress().getHostAddress();
        this.owner = owner;
        this.port = port;
        setupAndRun();
        
    }

    private void setupAndRun()   {
        //these just need a receiver thread with owner type of what was passed in (MessagingNode)
        try {
            TCPReceiverThread receiver = new TCPReceiverThread(socket, owner);
            Thread receiverThread = new Thread(receiver);
            receiverThread.start();
            
            
        } catch (IOException ioe) {
            System.out.println("Issue with creating a receiver for the PeerNode");
            System.out.println(ioe.getMessage());
        }
        
    }
}
