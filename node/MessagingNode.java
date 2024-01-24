package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Random;

import transport.TCPSender;
import transport.TCPServerThread;
import util.InputHandler;
import wireformats.*;

/* TODO - now DONE
 * declare fields for network connection
 * find our port number (use while loop or something -> need try catch)
 * once port number has been found, bind the server socket to that specific port!
 * after that, set our InetAddress -> this will be needed to register
 */

/* TODO - future 
 * write the request registry method
 * for this I will need to write the TCPsender and TCPreceiver in the transport folder
*/

public class MessagingNode {
    //fields for socket programming
    private static ServerSocket serverSocket;
    private static int serverPort;


    int messagesSent = 0;
    long messagesSentSum = 0;

    public int messagesReceived = 0;
    public long messagesReceivedSum = 0;

    int messagesRelayed = 0;    

    private static InetAddress regInetAddress;
    private static int registryPort;
    private static Socket registrySocket;

    private static TCPSender sender;

    public static ArrayList<RegisteredNode> registeredNodes = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        MessagingNode node = new MessagingNode();
        node.start(args);  
    }

    private void start(String[] args) throws IOException, InterruptedException {
        initiateServerSocket(); //server socket is made
        //create a listener for this guy for future messaging nodes
        
        regInetAddress = InetAddress.getByName(args[0]);
        System.out.println(regInetAddress);
        registryPort = Integer.parseInt(args[1]);
        registrySocket = new Socket(regInetAddress, registryPort);

        System.out.println("connected to server!");
        sender = new TCPSender(registrySocket);

        RegisteredNode registryConnectionNode = new RegisteredNode(registrySocket,this, registryPort); //this node is what you receive form registry on!

        sendRegisterRequest();

        
        System.out.println("Messaging node creting server thread listing on IP " + serverSocket.getInetAddress().getLocalHost().getHostAddress() + " and port " + serverSocket.getLocalPort());
        TCPServerThread server = new TCPServerThread(serverSocket, registeredNodes, this);
        Thread serverThread = new Thread(server);
        serverThread.start();

        InputHandler inputHanlder = new InputHandler();
        Thread inputHandlerThread = new Thread(inputHanlder,"Input handler Thread");
        inputHandlerThread.start();

        //messagingnodes need serverthreads too (to listen to for connections between other messagingNodes)

        inputHandlerThread.join();
        serverThread.join();
        
    }

    private static void initiateServerSocket() { 
        boolean found = false;
        int initialPort = 8080;
        while(!found) {
            try {
                serverSocket = new ServerSocket(initialPort);
                serverPort = initialPort;
                found = true;
            } catch(IOException ioe) {
                ++initialPort;
            }
        }
    }
    
    private static void sendRegisterRequest() throws IOException {
        System.out.println("Sending request with IP = " + InetAddress.getLocalHost().getHostAddress());
        System.out.println("and port = " + serverPort);
        RegisterRequest registerRequest = new RegisterRequest(InetAddress.getLocalHost().getHostAddress(), serverPort);
        byte[] registeryMessage = registerRequest.setBytes();
        sender.sendData(registeryMessage);
    }  

    public void addConnectionProtocol(ArrayList<String> peerNodeList) throws IOException {
        for(int i = 0; i < peerNodeList.size(); ++i) {
            //need to parse the IP and the INT
            String connectIP = peerNodeList.get(i).substring(0,13).trim();
            System.out.println("Node IP = " + connectIP);
            
            int connectPort = Integer.parseInt(peerNodeList.get(i).substring(13, peerNodeList.get(i).length())); 
            //connect the mf socket 
            Socket connectedNode = new Socket(connectIP, connectPort);
            //create a registered node and add it to the list
            RegisteredNode newConnection = new RegisteredNode(connectedNode, this, connectPort);
            registeredNodes.add(newConnection);
            //send a registration message to whoever you connected to so they can add YOU to the list
            //RegisterRequest notification = new RegisterRequest(InetAddress.getLocalHost().getHostAddress(), serverPort);
            //TCPSender sender = new TCPSender(connectedNode);
            //byte[] message = notification.setBytes();
            //sender.sendData(message);
            
        }

        System.out.println("All connections are established. Number of connections " + registeredNodes.size());
    }

    public void initiateTask(int rounds) {
        //im going to assume that every round is five messages
        Random rand = new Random();
        if(registeredNodes.size() > 0) {
            System.out.println(registeredNodes.get(0).ipAddress);
            System.out.println(registeredNodes.get(0).portNumber);
        }
        for(int i = 0; i < rounds; ++i) {
            for(int j = 0; j < 5; ++j) {
                int payload = rand.nextInt();
                Message msg = new Message(payload);
                try {
                    byte[] message = msg.setBytes();
                    TCPSender sender = new TCPSender(registeredNodes.get(0).socket);
                    sender.sendData(message);
                    messagesSent+=1;
                    messagesSentSum += payload;
                } catch(IOException ioe) {

                }

            }
        }

        System.out.println("TASK DONE");
        System.out.println("num sent = " + messagesSent);
        System.out.println("num received = " + messagesReceived);
        System.out.println("sum of sent = " + messagesSentSum);
        System.out.println("sum of received = " + messagesReceivedSum);
    }

    public void processMessage(int payload) {
        /*
         * in the future this will take a string as input
         * it will determine if its the sink by deleting -'s,
         *      then spliiting into a string array, and if its IP is the last index
         *       then it is the sink, if not, it will pass the message to the IP that is after its own!
         */

         messagesReceivedSum += payload;
         messagesReceived += 1;

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
}
