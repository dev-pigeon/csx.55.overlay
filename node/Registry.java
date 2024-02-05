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


public class Registry {
    //the values for port number and server address are passed from command line
    private static int port;
    private static InetAddress serverAddress;
    private static ServerSocket serverSocket;
    private static int numTaskComplete = 0;
    private static int numSummaryReceived = 0;
    private static ArrayList<String> summaryList = new ArrayList<>();;
  
    public  ArrayList<RegisteredNode> registeredNodes = new ArrayList<>();
    public ArrayList<String> linkMessages = new ArrayList<String>();

    private ConnectionMessageGenerator generator;

    private OverlayCreator overlayCreator;

    long totalSumSent = 0;
    long totalSumReceived = 0;
    int totalNumSent = 0;
    int totalNumReceived = 0;
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
       overlayCreator = new OverlayCreator(registeredNodes, linkMessages, numConnections);
       overlayCreator.buildOverlay();
       
       //call the generator to generate the connection lists for each node
       generator = new ConnectionMessageGenerator(registeredNodes);
       generator.generateConnectionMessages();
       
        //call send connection messages
        TCPSender sender;
        for(int i = 0; i < registeredNodes.size(); ++i) {
            //loop each indexes messages and send to their sockets!
            Messaging_Nodes_List message = new Messaging_Nodes_List(registeredNodes.get(i).connectionMessageList, registeredNodes.get(i).connectionMessageList.size());
            try {
                byte[] marshalledMessage = message.setBytes();
                sender = new TCPSender(registeredNodes.get(i).socket);
                sender.sendData(marshalledMessage);
            } catch(IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }

        overlayCreator.updateAllLinkWeights();
        
    }

    public void sendLinkWeights() {
        TCPSender sender;
        Link_Weights linkWeights = new Link_Weights(linkMessages);
        try {
            byte[] marshalledWeights = linkWeights.setBytes();
            for(int i = 0; i < registeredNodes.size(); ++i) {
                sender = new TCPSender(registeredNodes.get(i).socket);
                sender.sendData(marshalledWeights);
            }
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public void listRegisteredNodes() {
        for(int i = 0; i < registeredNodes.size(); ++i) {
            try {
                System.out.println(InetAddress.getByName(registeredNodes.get(i).ip).getHostName() + " on port " + registeredNodes.get(i).portNum);
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
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
       // System.out.println("number of nodes in registy  = "  + registeredNodes.size());
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
   private void pullTrafficSummary() throws IOException {
    PullTrafficSummary summaryRequest = new PullTrafficSummary();
   // System.out.println("sending pull traffic summary");
    byte[] summaryRequestBytes = summaryRequest.setBytes();
    for(RegisteredNode node : registeredNodes) {
        TCPSender sender = new TCPSender(node.socket);
        sender.sendData(summaryRequestBytes);
    }

   }

   public synchronized void storeTrafficSummary(int numSent, int numReceived, long sumSent, long sumReceived) {

    //System.out.println("receiving summary");
    totalNumSent += numSent;
    totalNumReceived += numReceived;

    totalSumSent += sumSent;
    totalSumReceived += sumReceived;
    String nodeSummary = "Node " + (Integer.toString(numSummaryReceived+1)) + " " + Integer.toString(numSent) + " " + Integer.toString(numReceived) + " " + Long.toString(sumSent) + " " + Long.toString(sumReceived);
    summaryList.add(nodeSummary);
    ++numSummaryReceived;
    if(numSummaryReceived == registeredNodes.size()) {
        String finalSummary = "Sum " + Integer.toString(totalNumSent) + " "  + Integer.toString(totalNumReceived) + " " + Long.toString(totalSumSent) + " " + Long.toString(totalSumReceived);
        printTrafficSummary();
        System.out.println(finalSummary);
        numSummaryReceived = 0;
    }

   }

   private synchronized void printTrafficSummary() {
    for(String summary : summaryList) {
        System.out.println(summary);
    }
   }

   public void listLinkWeights() {
    for(int i = 0; i < linkMessages.size(); ++i) {
        System.out.println(linkMessages.get(i));
    }
}

}