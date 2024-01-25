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

        /* 
         * the reason I am passing 'this' into these threads is so these threads have a reference back to their owner
         * in this case is Registry, its so registryCLI can call methods that the registry owns
         * it allows the server listener to make RegisteredNodes as well, when R nodes are made, they pass instances of 
         * THEMSELVES into their receiver threads, it is so when they receive a register request from their sister node (held in the instance of
         * a messaging node that connected with the registry) they can pass themselves into a public static function in Registry that will verify them
         * and add them to the list if neccessary, and send their registration response message, I think this is more elegant
         */
       
        RegisteryCLI registeryCLI = new RegisteryCLI(this);
        Thread registryCLIThread = new Thread(registeryCLI);
        registryCLIThread.start();
        
       
        TCPServerThread serverListener = new TCPServerThread(serverSocket, this); 
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
            ArrayList<String> connectionIPList = new ArrayList<>();
            ArrayList<Integer> connectionPortList = new ArrayList<>();
            connectionIPList.add(registeredNodes.get(1).ipAddress);
            connectionPortList.add(registeredNodes.get(1).portNum);
                        
            Messaging_Nodes_List messaging_Nodes_List = new Messaging_Nodes_List(connectionIPList, connectionPortList, connectionIPList.size());
            byte[] marshalledData = messaging_Nodes_List.setBytes();
            sender.sendData(marshalledData);
            
            //both lists will ALWAYS BE THE SAME SIZE

            
            
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void listRegisteredNodes() {
        for(int i = 0; i < registeredNodes.size(); ++i) {
            System.out.println(registeredNodes.get(i).ipAddress + " on port " + registeredNodes.get(i).socket.getPort());
        }
    } 
    
    public void initiateMessagingNodes(int rounds)  {
        //loop our little list and send message to em all
        Task_Initiate task = new Task_Initiate(rounds);
        
        try {
            byte[] taskMessage = task.setBytes();
            System.out.println("writing to " + registeredNodes.size() + " nodes");
            for(RegisteredNode node : registeredNodes) {
                TCPSender sender = new TCPSender(node.socket);
                sender.sendData(taskMessage);
            }
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        
    }

    public static void ValidateNode(RegisteredNode potentialNode, String requestIP, int requestPort) {

        //check if the IP in the object/socket is diff from the request, if so kill his ass
        System.out.println("i was successfully called");
        if(!potentialNode.socket.getInetAddress().getHostAddress().equals(requestIP)) {
            String error = "ERROR: Mismatch in IP of socket and IP sent in the request message.";
            GenerateRegistrationResponse(potentialNode, (byte) 0, error);
            return;
        }
        //check if it is already registered, loop our list of current guys and check if this one has a mathcing IP and port, if so KILL HIS ASS
        for(int i = 0; i < registeredNodes.size(); ++i) {
            if((registeredNodes.get(i).ipAddress).equals(requestIP) && (registeredNodes.get(i).portNum == requestPort)) {
                String error = "ERROR: This IP address and port are already registered with the registy.";
                GenerateRegistrationResponse(potentialNode, (byte) 0, error);
                return;
            }
        }

        //we are down here so we call RegisterNode
        RegisterNode(potentialNode, requestPort);

    }

    private static void GenerateRegistrationResponse(RegisteredNode potentialNode, byte status, String message) {
        try {
            TCPSender sender = new TCPSender(potentialNode.socket);
            RegisterResponse response = new RegisterResponse(status, message);
            byte[] marshalledResponse = response.setBytes();
            sender.sendData(marshalledResponse);
        } catch (IOException ioe) {
            System.out.println("error sending the registration response");
            System.out.println(ioe.getMessage());
        }
   }

   private static void RegisterNode(RegisteredNode potentialNode, int registerPort) {
        //update that guys port
        potentialNode.setPortNum(registerPort);
        //add him and generate the response
        registeredNodes.add(potentialNode);
        String message = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + Integer.toString(registeredNodes.size()) + ")";
        GenerateRegistrationResponse(potentialNode, (byte)1, message);
   }
}