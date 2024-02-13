package csx55.overlay.transport;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;



import csx55.overlay.node.RegisteredNode;
import csx55.overlay.wireformats.*;
import csx55.overlay.node.*;


public class TCPServerThread implements Runnable {
    //this will be spawned by both entities and will simply be the listener of connections (not sure how itll work like addding things lmao)
    
    ServerSocket serverSocket;
    //need a field for owner tpe
    private Object owner;

    TCPSender sender;

    boolean accepted = false;

    public TCPServerThread(ServerSocket serverSocket, Object owner) {
        this.serverSocket = serverSocket;
        this.owner = owner;

    }

    @Override
    public void run() {
        while(true) {
           Socket socket = acceptConnections();

           if(owner instanceof Registry) {
            //create a registered node
            try {
               // System.out.println("creating registered node");
                RegisteredNode potentialNode = new RegisteredNode(socket, owner, socket.getInetAddress().getHostAddress(), socket.getPort());
                potentialNode.setUpandRun();
            } catch (IOException ioe) {
    
                System.out.println("Problem in creating a registered node");
                System.out.println(ioe.getMessage());
            }
            
           } else if(owner instanceof MessagingNode) {
             //System.out.println("creating peer node");
             try {
                //WE ARE SETTING PORT = TO ZERO HERE BECAUSE WE DONT REALLY NEED TO KNOW THE PORT SINCE WE HAVE BEEN THE
                //THAT WAS CONNECTED TO
                //turn this to registered nodes
                RegisteredNode peerNode = new RegisteredNode(socket, owner,socket.getInetAddress().getHostAddress(), socket.getPort());
                peerNode.setUpandRun();
                ((MessagingNode)owner).addConnection(peerNode);
            } catch (IOException ioe) {
                System.out.println("Problem in creating a registered node");
                System.out.println(ioe.getMessage());
            }
           } 
        }

    }

    private Socket acceptConnections() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return socket;
    }
}
