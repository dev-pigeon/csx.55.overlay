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
    private String owner;

    TCPSender sender;

    boolean accepted = false;

    public TCPServerThread(ServerSocket serverSocket, ArrayList<RegisteredNode> registeredNodes, String owner) {
        this.serverSocket = serverSocket;
        this.registeredNodes = registeredNodes;
        this.owner = owner;
    }

    @Override
    public void run() {
        while(true) {
           Socket socket = acceptConnections();
           try {

             readRegistrationEvent(socket);

             if(accepted) {

                System.out.println("a messaging node was sucessfully registered");
                
                RegisteredNode node = new RegisteredNode(socket, owner);
                addToList(node);
                //call generate response message
                sender = new TCPSender(socket);
                String responseInfo = "Registration request successful. Then number of nodes currently in the overlay is (" + registeredNodes.size() + ")";
                generateReponse(responseInfo, accepted);
                accepted = false;
             } else {
                sender = new TCPSender(socket);
                String response = "ERROR: Invalid registration credentials";
                generateReponse(response, false);
             }
            } catch (IOException e) {
            // TODO Auto-generated catch block
                e.printStackTrace();
            }
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

    private void addToList(RegisteredNode registeredNode) {
        synchronized(registeredNodes) {
            registeredNodes.add(registeredNode);
        }
    }

    private RegisteredNode createRegisteredNode(Socket socket) {
        RegisteredNode registeredNode = null;
        try {
            registeredNode = new RegisteredNode(socket, owner);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return registeredNode;
    }

    private void readRegistrationEvent(Socket socket) throws IOException {
        //the "origin IP" for that check can be gotten from this socket
        //i think?
        DataInputStream din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        String socketIP = socket.getInetAddress().getHostAddress();

        int length = din.readInt();
        byte[] marshalledData = new byte[length];
        din.readFully(marshalledData,0,length);
        System.out.println("Server thread: bytes have been read fully with length = " + length);
        System.out.println(marshalledData);
        //we dint need the type 
        RegisterRequest request = new RegisterRequest();
        //then get the bytes
        request.getBytes(marshalledData);
        //examine the data
        if(owner.equals("Registry")) {
            //check if it is a mismatch
            if(!request.ipAddress.equals(socketIP)) {
                //call generate response message
                return;
                 
            } else {
                for(int i = 0; i < registeredNodes.size(); ++i) {
                    if((registeredNodes.get(i).ipAddress.equals(request.ipAddress)) && registeredNodes.get(i).portNumber == socket.getPort()) {
                        return;
                    }
                }
            }           
        } else { //case of messaging node receiving connection from other messaging nodes
            
        }
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
}
