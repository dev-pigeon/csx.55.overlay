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
    private static int numTaskComplete = 0;
    private static int numSummaryReceived = 0;
    private static ArrayList<String> summaryList = new ArrayList<>();;
  
    public static ArrayList<RegisteredNode> registeredNodes = new ArrayList<>();

    private OverlayCreator overlayCreator;
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
                //System.out.println("incrementing");
                ++initialPort;
            }
        }
    }

    public void setupOverlayProtocol(int numConnections) {
       overlayCreator = new OverlayCreator(registeredNodes, numConnections);
       overlayCreator.buildOverlay();
       //call make connectionMessages from here (but it is overlaycreator and make it return something)
       
    }

    public void listRegisteredNodes() {
        for(int i = 0; i < registeredNodes.size(); ++i) {
            System.out.println(registeredNodes.get(i).ip + " on port " + registeredNodes.get(i).socket.getPort());
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

    public synchronized void ValidateNode(RegisteredNode potentialNode, String requestIP, int requestPort) {

        //check if the IP in the object/socket is diff from the request, if so kill his ass
        //System.out.println("i was successfully called");
        if(!potentialNode.socket.getInetAddress().getHostAddress().equals(requestIP)) {
            String error = "ERROR: Mismatch in IP of socket and IP sent in the request message.";
            GenerateRegistrationResponse(potentialNode, (byte) 0, error);
            return;
        }
        //check if it is already registered, loop our list of current guys and check if this one has a mathcing IP and port, if so KILL HIS ASS
        for(int i = 0; i < registeredNodes.size(); ++i) {
            //System.out.println(i + " " + registeredNodes.get(i).portNum);
            //System.out.println(registeredNodes.get(i).ip);

            if((registeredNodes.get(i).ip).equals(requestIP) && (registeredNodes.get(i).portNum == requestPort)) {
                String error = "ERROR: This IP address and port are already registered with the registy.";
                GenerateRegistrationResponse(potentialNode, (byte) 0, error);
                return;
            }
        }
        RegisterNode(potentialNode, requestPort);

    }

    private void GenerateRegistrationResponse(RegisteredNode potentialNode, byte status, String message) {
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

   private void RegisterNode(RegisteredNode potentialNode, int registerPort) {
        //update that guys port
        potentialNode.setPortNum(registerPort);
        //add him and generate the response
        registeredNodes.add(potentialNode);
        System.out.println("number of nodes in registy  = "  + registeredNodes.size());
        String message = "Registration request successful. The number of messaging nodes currently constituting the overlay is (" + Integer.toString(registeredNodes.size()) + ")";
        GenerateRegistrationResponse(potentialNode, (byte)1, message);
   }

   public synchronized void checkDeregisterRequest(String ip, int port, RegisteredNode node) {
        if(!registeredNodes.contains(node)) {
            String message = "ERROR: Deregistration Request failed. Node was not in registry.";
            GenerateRegistrationResponse(node, (byte)0, message);
        } else if(!ip.equals(node.socket.getInetAddress().getHostAddress())) {
            System.out.println("REQUEST IP = " + ip + " and node socket IP = " + node.socket.getInetAddress().getHostAddress());
            String message = "Error: Deregistration Request failed. IP of request and sock differ";
            GenerateRegistrationResponse(node, (byte)0, message);
        }

        //only send messages for failure, just unadd the node now
        registeredNodes.remove(node);
    
   }

   public synchronized void checkTaskComplete() {
    ++numTaskComplete;
   // System.out.println("number of task completes is " + numTaskComplete);
    if(numTaskComplete == registeredNodes.size()) {
        try {
            Thread.sleep(15000);
            pullTrafficSummary();
        } catch(InterruptedException  | IOException E ) {
            System.out.println(E.getMessage());
        }
    }

   }
   private static void pullTrafficSummary() throws IOException {
    PullTrafficSummary summaryRequest = new PullTrafficSummary();
   // System.out.println("sending pull traffic summary");
    byte[] summaryRequestBytes = summaryRequest.setBytes();
    for(RegisteredNode node : registeredNodes) {
        TCPSender sender = new TCPSender(node.socket);
        sender.sendData(summaryRequestBytes);
    }

   }

   public /*synchronized*/ void storeTrafficSummary(String summary) {

    //System.out.println("receiving summary");
    String nodeSummary = "Node " + Integer.toString(numSummaryReceived) + " " + summary;
    summaryList.add(nodeSummary);
    ++numSummaryReceived;
   // System.out.println("number of summaries = " + numSummaryReceived);
    if(numSummaryReceived == registeredNodes.size()) {
        printTrafficSummary();
    }

   }

   private synchronized void printTrafficSummary() {
    for(String summary : summaryList) {
        System.out.println(summary);
    }
   }

}