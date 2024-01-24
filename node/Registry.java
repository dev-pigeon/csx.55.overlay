package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import util.*;
import transport.*;
import wireformats.*;



 //the server thread will listen for connections from possible messaging nodes
        //upon a successful TCP connection the serverThread will create a registered node object
        //this registered node object will be put into the list of registered nodes
        //note there will be a RegistryCLI and a MessagingNodeCLI seperately in the util folder
        //RegistryCLI will also contain the list of these registered node objects
        //this is because at least one command requires the registry to send information to the messaging nodes
            //who's connections are held by the socket in the registered node object in the list
        //IMPORTANT NOTE -> LINK WEIGHTS ARE NOT KNOWN BY EACH MSGING NODE
            //the first time that node A wants to send a packet to node X,
            //it will query the Registry for the shortest path with a wireformat containing its IP and the IP of where it wants to go
            //from there the reigstry will compute djkstras algortihm and return a String[] of IP addresses that is the shortest route
            //this will then be sent back to the msging node and they'll route the package
                //any other time Node A wants to go to node E, it will look up the route in the cache
        //therefor, the receiver will need a reference to the list of Registered nodes
            //and registered node objects only need to have a socket, ip, and port because they're used for communication
            //a registered node can be identified my another msging node by its IP, or its spot in the list since the nodes
            //are to send messages to RANDOM other msging nodes and for what I just explained above, Registered nodes dont need weight fields,
            //theyre just used for sending shit to other messagingnodes

public class Registry {
    //the values for port number and server address are passed from command line
    private static int port;
    private static InetAddress serverAddress;
    private static ServerSocket serverSocket;
  
    public static ArrayList<RegisteredNode> registeredNodes = new ArrayList<>();
    public static void main(String[] args) throws IOException, InterruptedException {
       
       Registry registry = new Registry();
       registry.start(args);
        
    }

    private void start(String[] args) throws InterruptedException, UnknownHostException {
        port = Integer.parseInt(args[0]);

        initiateServerSocket();
        serverAddress = InetAddress.getLocalHost();
        System.out.println("port: " + serverSocket.getLocalPort() + " address: " + serverAddress.getHostAddress());

       
        RegisteryCLI registeryCLI = new RegisteryCLI(this);
        Thread registryCLIThread = new Thread(registeryCLI);
        registryCLIThread.start();
        
        TCPServerThread serverListener = new TCPServerThread(serverSocket, registeredNodes, this); //make this pass an instance of itself as well lmao
        Thread tcpServerThread = new Thread(serverListener,"TCPServerThread");
        tcpServerThread.start();

        tcpServerThread.join();
        registryCLIThread.join();
    }

    private static void initiateServerSocket() { 
        boolean found = false;
        int initialPort = 8080;
        while(!found) {
            try {
                serverSocket = new ServerSocket(initialPort);
                found = true;
            } catch(IOException ioe) {
                System.out.println("incrementing");
                ++initialPort;
            }
        }
    }

    public void setupOverlayProtocol(int numConnections) {
        System.out.println("there are " + registeredNodes.size() + " nodes ");
        System.out.println("received that this should happen with num = " + numConnections);
        //connect the node in position 0 to position
        try {
            TCPSender sender = new TCPSender(registeredNodes.get(0).socket);
            //I have a sender for message A, so I need to send B's info 
            //populate the list
            ArrayList<String> peerNodeList = new ArrayList<>();
            String nodeInfo = registeredNodes.get(1).ipAddress + " " + Integer.toString(registeredNodes.get(1).portNumber);
            
            peerNodeList.add(nodeInfo);
            Messaging_Nodes_List messaging_Nodes_List = new Messaging_Nodes_List(peerNodeList, peerNodeList.size());
            byte[] marshalledData = messaging_Nodes_List.setBytes();
            sender.sendData(marshalledData);

            
            
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void listRegisteredNodes() {
        for(int i = 0; i < registeredNodes.size(); ++i) {
            System.out.println(registeredNodes.get(i).ipAddress + " on port " + registeredNodes.get(i).portNumber);
        }
    } 
    
    public void createAndAddNode(Socket socket, RegisterRequest request) {
        try {
            System.out.println("Registry has been called to create a node with port number = " + request.portNumber);
            RegisteredNode registeredNode = new RegisteredNode(socket, this, request.portNumber);
            registeredNodes.add(registeredNode);
            TCPSender registerConfirmation = new TCPSender(socket);
            RegisterResponse registerResponse = new RegisterResponse((byte)1, "Registration successful, there are (" + registeredNodes.size() + ") nodes in the overlay");
            byte[] marhalledData = registerResponse.setBytes();
            registerConfirmation.sendData(marhalledData);
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        } 
    }

    public void initiateMessagingNodes(int rounds)  {
        //loop our little list and send message to em all
        Task_Initiate task = new Task_Initiate(rounds);
        
        try {
            byte[] taskMessage = task.setBytes();
            for(RegisteredNode node : registeredNodes) {
                TCPSender sender = new TCPSender(node.socket);
                sender.sendData(taskMessage);
            }
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        
    }
}