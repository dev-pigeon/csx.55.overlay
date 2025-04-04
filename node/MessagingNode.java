package csx55.overlay.node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Random;

import csx55.overlay.dijkstra.*;
import csx55.overlay.dijkstra.Cache.CacheObject;
import csx55.overlay.transport.TCPSender;
import csx55.overlay.transport.TCPServerThread;
import csx55.overlay.util.InputHandler;
import csx55.overlay.util.MsgNodeCLI;
import csx55.overlay.util.OverlayCreator;
import csx55.overlay.wireformats.*;

public class MessagingNode {
    //fields for socket programming
    private static ServerSocket serverSocket;
    private static int serverPort;


    int messagesSent = 0;
    long messagesSentSum = 0;

    public int messagesReceived = 0;
    public long messagesReceivedSum = 0;

    TCPServerThread server;

    int messagesRelayed = 0;    

    private static InetAddress regInetAddress;
    private static int registryPort;
    private static Socket registrySocket;

    private static TCPSender sender;

    public static HashMap<RegisteredNode, Integer> peerNodes = new HashMap<>();

    RegisteredNode registryConnectionNode;

     static MessagingNode self;

    public ArrayList<RegisteredNode> masterList = new ArrayList<>();

    ArrayList<String> linkMessages = new ArrayList<>();

     OverlayCreator overlayCreator = new OverlayCreator(masterList, linkMessages, 0);

     Cache routeCache = new Cache();

     Djikstra djikstra;

     MsgNodeCLI cli;



    public static void main(String[] args) throws IOException, InterruptedException {
        self = new MessagingNode();
        self.start(args);  
    }

    private void start(String[] args) throws IOException, InterruptedException {
        initiateServerSocket(); //server socket is made
        //create a listener for this guy for future messaging peerNodes
        
        regInetAddress = InetAddress.getByName(args[0]);
        System.out.println(regInetAddress);
        registryPort = Integer.parseInt(args[1]);
        registrySocket = new Socket(regInetAddress, registryPort);

        //System.out.println("connected to server!");
        sender = new TCPSender(registrySocket);

        registryConnectionNode = new RegisteredNode(registrySocket, this, regInetAddress.getHostAddress(), registryPort); //this node is what you receive form registry on!
        registryConnectionNode.setUpandRun();
        sendRegisterRequest();

        
        //System.out.println("Messaging node creting server thread listing on IP " + serverSocket.getInetAddress().getLocalHost().getHostAddress() + " and port " + serverSocket.getLocalPort());
        server = new TCPServerThread(serverSocket, this);
        Thread serverThread = new Thread(server);
        serverThread.start();

        cli = new MsgNodeCLI(self);
         Thread cliThread = new Thread(cli);
         cliThread.start();

        //messagingnodes need serverthreads too (to listen to for connections between other messagingNodes)
    }

    private static void initiateServerSocket() { 
        boolean found = false;
        int initialPort = 8080;
        while(!found) {
            try {
                serverSocket = new ServerSocket(initialPort);
                serverPort = initialPort;
                System.out.println(serverPort);
                found = true;
            } catch(IOException ioe) {
                ++initialPort;
            }
        }
    }
    
    private static void sendRegisterRequest() throws IOException {
        RegisterRequest registerRequest = new RegisterRequest(InetAddress.getLocalHost().getHostAddress(), serverPort);
        byte[] registeryMessage = registerRequest.setBytes();
        sender.sendData(registeryMessage);
    }  

    private static void sendPeerPort(RegisteredNode newPeer) {
        try {
            TCPSender tempSender = new TCPSender(newPeer.socket);
                PeerPortNumber port = new PeerPortNumber(serverPort);
                byte[] message = port.setBytes();
                tempSender.sendData(message);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    public void addConnectionProtocol(ArrayList<String> connectionList) throws IOException{


        for(int i = 0; i < connectionList.size(); ++i) {
            
            String address = parseIPAddress(connectionList.get(i));
            int port = parsePortNumber(connectionList.get(i));
            Socket connectedNode = new Socket(address.trim(), port);

            RegisteredNode newConnection;
            try {
                newConnection = new RegisteredNode(connectedNode, this, connectedNode.getInetAddress().getHostAddress(), connectedNode.getPort());
                addConnection(newConnection);
                newConnection.setUpandRun();
                sendPeerPort(newConnection);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        
        try {
            Thread.sleep(1500);
            System.out.println("All connections are established. Number of connections " + peerNodes.size());
        } catch(InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public void addConnection(RegisteredNode node) {
        peerNodes.put(node,0);
    }

    /*
     * extracts the port number from a connection message by
     * finding the findex of : and going i + 1 till the end of the string
     */
    private int parsePortNumber(String message) {
        int port = 0;
        int colonIndex = message.indexOf(":");
        port = Integer.parseInt(message.substring(colonIndex + 1, message.length()));
        return port;
    }

    /*
     * same as above but it goes from 0-> index of :
     */
    private String parseIPAddress(String message) {
        int colonIndex = message.indexOf(":");
        String IP = message.substring(0, colonIndex);
        return IP;
    }

    public synchronized void initiateTask(int rounds) {
        //im going to assume that every round is five messages
        //calculate all the paths
        djikstra = new Djikstra(routeCache, masterList); //now cache has all of my routes
        RegisteredNode start = findYourselfInOverlay();
        djikstra.findAllRoutes(start);
       Random rand = new Random();
        for(int i = 0; i < rounds; ++i) {
            for(int j = 0; j < 5; ++j) {
                int payload = rand.nextInt();
                try {  
                    
                    RegisteredNode sink = getRandomNode(); //gets from the masterlist which is what the cache uses
                   // System.out.println("DEBUG: sink that was found is = " + sink.ip + ":" + sink.portNum);
                    CacheObject route = routeCache.findForMessaging(sink);
                   // System.out.println("just got this route " + route);
                    String routeString = routeCache.convertRouteToString(route);
                   // System.out.println("route string = " + routeString);
                    routeString = routeString.replace("~", " ");
                   
                    String[] routeArr = routeString.split(" ");
                    //index 0 is you, index 1 is the weight, index 2 is what you want
                    
                    //System.out.println();

                    RegisteredNode neighborToSend = findNodeToSendTo(routeArr[2]);

                    String routeToSend = routeArrToString(2, routeArr);
                   // System.out.println("SENDING ROUTE TO SEND " + routeToSend);
                    Message msg = new Message(payload, routeToSend);
                    byte[] message = msg.setBytes();
                    TCPSender sender = new TCPSender(neighborToSend.socket);
                    sender.sendData(message);
                    messagesSent+=1;
                    messagesSentSum += payload;
                } catch(IOException ioe) {

                }
            }
        }
        try {
            Thread.sleep(5000);
            //System.out.println("sending task complete");
            sendTaskComplete();
        } catch (InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
        
    }

    public RegisteredNode parseFirstIP(int index) {
        RegisteredNode firstNode = null;
        String IP = masterList.get(index).ip;

        for(RegisteredNode entry : peerNodes.keySet()) {
            if(entry.ip.equals(IP)) {
                firstNode = entry;
                break;
            }
        }
        return firstNode;
    }

    public RegisteredNode getRandomNode() {
        Random rand = new Random();
        int index = -1;
        RegisteredNode selectedNode = null;
        while(true) {
            index = rand.nextInt(masterList.size());
            
            try {
                selectedNode = masterList.get(index);
                if(!selectedNode.ip.equals(InetAddress.getLocalHost().getHostAddress()) || selectedNode.portNum != serverPort) {
                    break;
                } 
            } catch (UnknownHostException e) {
                e.printStackTrace();
            }
        }


        return selectedNode; 

    }

    public void sendDeregisterRequest() throws IOException {
        DeregisterRequest request = new DeregisterRequest(InetAddress.getLocalHost().getHostAddress(), serverPort);
        byte[] marshalledRequest = request.setBytes();
        sender.sendData(marshalledRequest);
        try {
            Thread.sleep(2000);
            System.exit(0);
        } catch(InterruptedException e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized void sendTrafficSummary() {
        //System.out.println("ive been asked for my summary and I am sending it");
        TrafficSummary summary = null;
        try {
            summary = new TrafficSummary(messagesSent, messagesReceived, messagesRelayed, messagesSentSum, messagesReceivedSum);
            byte[] marshalledSummary = summary.setBytes();
            sender = new TCPSender(registrySocket);
            sender.sendData(marshalledSummary);
           // System.out.println("kust sent summary");
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    private synchronized void sendTaskComplete() throws IOException {
        TaskComplete message = new TaskComplete(InetAddress.getLocalHost().getHostAddress(), serverPort);
        byte[] marshalledMessage = message.setBytes();
        sender.sendData(marshalledMessage);
    }

    public void linkWeightProtocol(RegisteredNode nodeOne, RegisteredNode nodeTwo, int weight, int numLeft) throws UnknownHostException {//IP and ports are good here
        //checl for base case 
        if(masterList.size() == 0) {
            masterList.add(nodeOne);
            masterList.add(nodeTwo);
        }

        RegisteredNode realNodeOne = findNode(nodeOne);
        RegisteredNode realNodeTwo = findNode(nodeTwo);
        overlayCreator.formConnection(realNodeOne, realNodeTwo, weight);
        if(numLeft == 0) {
            //i am gonna run mu djikstra's algorithm here so that everythign is pre cached
            System.out.println("Link weights received and processed. Ready to send messages.");
        }
        
    }

    public void listWeights() throws UnknownHostException {
       routeCache.displayCachedRoutes();
    }

    private RegisteredNode findNode(RegisteredNode node) {
        RegisteredNode realNode = null;
        for(int i = 0; i < masterList.size(); ++i) {
            if(masterList.get(i).ip.equals(node.ip) && masterList.get(i).portNum == node.portNum) {
                realNode = masterList.get(i);
                return realNode;
            }
        }
        //node was not in master lst, add it
        masterList.add(node);
        return node;
    }

    public void testDjikstra() {
        Djikstra djikstra = new Djikstra(routeCache, masterList);
        RegisteredNode start = findYourselfInOverlay();
        djikstra.findAllRoutes(start);
    }

    private RegisteredNode findYourselfInOverlay() {
        RegisteredNode yourself = null;
        for(RegisteredNode node : masterList) {
            try {
                if(node.ip.equals(InetAddress.getLocalHost().getHostAddress()) && node.portNum == serverPort) {
                    yourself = node;
                    break;
                }
            } catch (UnknownHostException e) {
                // TODO Auto-generated catch block
                System.out.println(e.getMessage());
            }
        }
        return yourself;
    }

    private RegisteredNode findNodeToSendTo(String ipPort) throws UnknownHostException {
        String IP = InetAddress.getByName(parseIPAddress(ipPort)).getHostAddress();
        int port = parsePortNumber(ipPort);
        RegisteredNode neighborToSend = null;
        for(RegisteredNode entry : peerNodes.keySet()) {
            if(entry.ip.equals(IP) && entry.portNum == port) {
                neighborToSend = entry;
                break;
            }
        }
        return neighborToSend;
    }

    private String routeArrToString(int offset, String[] routeArr) {
        StringBuilder sb = new StringBuilder();

        for(int i = offset; i < routeArr.length; ++i) {
            sb.append(routeArr[i] + " ");
        }
        return sb.toString();
    }

    public synchronized void processMessage(String route, int payload) {
        //first need to check if you are the sink
        String[] routeArr = route.split(" ");
       // System.out.println(route);
        if(routeArr.length == 1) {
            
            messagesReceived+=1;
            messagesReceivedSum+=payload;
            //you are the sink
        } else {
            //split into an array
           
            //the node we need to send to is at index 0 in our array
            try {
           // System.out.println("FINDING ROUTING NODE WITH " + routeArr[2]);
            RegisteredNode routingNode = findNodeToSendTo(routeArr[2]);
            String routeToSend = routeArrToString(2, routeArr);

            Message msg = new Message(payload, routeToSend);
            byte[] marshalledMessage = msg.setBytes();

            //System.out.println("ROUTING MESSAGE " + routeToSend);

            TCPSender sender = new TCPSender(routingNode.socket); 
            sender.sendData(marshalledMessage);
            messagesRelayed +=1;
            } catch(IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }

    }

    public void removeFailedNode(RegisteredNode nodeFailed) {
        if(nodeFailed.equals(registryConnectionNode)) {
            System.out.println("ERROR: Lost connection with registry, terminating process...");
            //shutDown()
            System.exit(0);
        } else {
            System.out.println("failed and was not registry");
        }
    }

}