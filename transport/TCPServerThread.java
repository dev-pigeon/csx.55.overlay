package transport;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import javax.xml.crypto.Data;

import node.RegisteredNode;
import wireformats.*;
import node.*;


public class TCPServerThread implements Runnable {
    //this will be spawned by both entities and will simply be the listener of connections (not sure how itll work like addding things lmao)
    
    ServerSocket serverSocket;
    ArrayList<RegisteredNode> registeredNodes;
    //need a field for owner tpe
    private Object owner;

    TCPSender sender;

    boolean accepted = false;

    public TCPServerThread(ServerSocket serverSocket, Object owner) {
        this.serverSocket = serverSocket;
        this.registeredNodes = registeredNodes;
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
                RegisteredNode potentialNode = new RegisteredNode(socket);
            } catch (IOException ioe) {
                
                System.out.println("Problem in creating a registered node");
                System.out.println(ioe.getMessage());
            }
            
           } else if(owner instanceof MessagingNode) {
             //System.out.println("creating peer node");
             try {
                //WE ARE SETTING PORT = TO ZERO HERE BECAUSE WE DONT REALLY NEED TO KNOW THE PORT SINCE WE HAVE BEEN THE
                //THAT WAS CONNECTED TO
                PeerNode peerNode = new PeerNode(socket, (MessagingNode)owner, 0);
                ((MessagingNode)owner).peerNodes.add(peerNode);
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
           }

           
            //all commented code below this point was dumb and a bad implementation
            //we are going to make this much simpler,
            //no more need for registered node list
            //this class will have ONE responsibility, to accept connects, and make the proper node type, thats it.
            //the proper node types will handle themselves

            
        }
    }

     

    private Socket acceptConnections() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
            System.out.println("a connection has been accepted!");
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return socket;
    }

    /*

    
    private void readRegistrationEvent(Socket socket) throws IOException {
        //the "origin IP" for that check can be gotten from this socket
        //i think?
        DataInputStream din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        String socketIP = socket.getInetAddress().getHostAddress();

        int length = din.readInt();
        byte[] marshalledData = new byte[length];
        din.readFully(marshalledData,0,length);
        System.out.println(marshalledData);
        //we dint need the type 
        RegisterRequest request = new RegisterRequest();
        //then get the bytes
        request.getBytes(marshalledData);



        

        //examine the data
        if(owner instanceof Registry) {
            //check if it is a mismatch
            if(!request.ipAddress.equals(socketIP)) {
                sender = new TCPSender(socket);
                String response = "ERROR: Invalid registration credentials";
                generateReponse(response, false);
                 
            } else {
                for(int i = 0; i < registeredNodes.size(); ++i) {
                    if((registeredNodes.get(i).ipAddress.equals(request.ipAddress)) && registeredNodes.get(i).portNumber == request.portNumber) {
                        sender = new TCPSender(socket);
                        String response = "ERROR: Invalid registration credentials";
                        generateReponse(response, false);
                    }
                }
            }
            
            //in the instance of getting a messaging node registered with the server we have passed checks, call create and add
            ((Registry)owner).createAndAddNode(socket, request);

        } else if (owner instanceof MessagingNode){ //case of messaging node receiving connection from other messaging nodes
            //make sure they get added to the correct list?\\
            System.out.println("messenging guy detected");
            String message = "you have connected with another messaging node";
            sender = new TCPSender(socket);
            generateReponse(message, true);
            ((MessagingNode) owner).createAndAddNode(socket, request);
        }
        //call create and add 
        accepted = true;  
    }

    private void generateReponse(String message, boolean success) throws IOException {
        System.out.println("sending response message");
        RegisterResponse registerResponse = null;
        if(success) {
            registerResponse = new RegisterResponse((byte)1, message);
        } else {
            registerResponse = new RegisterResponse((byte)0, message);
        }

        byte[] marshalledMessage = registerResponse.setBytes();
        sender.sendData(marshalledMessage);

    }

    */
}
