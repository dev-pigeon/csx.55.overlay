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
import util.MsgNodeCLI;
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

   public ArrayList<RegisteredNode> nodes = new ArrayList<>();

    RegisteredNode registryConnectionNode;

     static MessagingNode self;



    public static void main(String[] args) throws IOException, InterruptedException {
         self = new MessagingNode();
        self.start(args);  
    }

    private void start(String[] args) throws IOException, InterruptedException {
        initiateServerSocket(); //server socket is made
        //create a listener for this guy for future messaging nodes
        
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
        TCPServerThread server = new TCPServerThread(serverSocket, this);
        Thread serverThread = new Thread(server);
        serverThread.start();

         MsgNodeCLI cli = new MsgNodeCLI(self);
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

    public void addConnectionProtocol(ArrayList<String> connectionList) throws IOException{


        for(int i = 0; i < connectionList.size(); ++i) {
            
            String address = parseIPAddress(connectionList.get(i));
            int port = parsePortNumber(connectionList.get(i));
            Socket connectedNode = new Socket(address.trim(), port);
           
            RegisteredNode newConnection;
            try {
                newConnection = new RegisteredNode(connectedNode, this, connectedNode.getInetAddress().getHostAddress(), connectedNode.getPort());
                nodes.add(newConnection);
                newConnection.setUpandRun();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        System.out.println("All connections are established. Number of connections " + nodes.size());
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

    public void initiateTask(int rounds) {
        //im going to assume that every round is five messages
        Random rand = new Random();
       
        for(int i = 0; i < rounds; ++i) {
            for(int j = 0; j < 5; ++j) {
                int payload = rand.nextInt();
                Message msg = new Message(payload);
                try {
                    byte[] message = msg.setBytes();
                    TCPSender sender = new TCPSender(nodes.get(0).socket);
                    sender.sendData(message);
                    self.messagesSent+=1;
                    self.messagesSentSum += payload;
                } catch(IOException ioe) {

                }

            }
        }
        try {
            Thread.sleep(3000);
            //System.out.println("sending task complete");
            self.sendTaskComplete();
        } catch (InterruptedException | IOException e) {
            // TODO Auto-generated catch block
            System.out.println(e.getMessage());
        }
        
    }

    public void processMessage(int payload) {
         self.messagesReceivedSum += payload;
         self.messagesReceived += 1;
    }

    public void sendDeregisterRequest() throws IOException {
        DeregisterRequest request = new DeregisterRequest(InetAddress.getLocalHost().getHostAddress(), serverPort);
        byte[] marshalledRequest = request.setBytes();
        sender.sendData(marshalledRequest);
    }

    public void sendTrafficSummary() {
        //System.out.println("ive been asked for my summary and I am sending it");
        TrafficSummary summary = null;
        try {
            summary = new TrafficSummary(self.messagesSent, self.messagesReceived, self.messagesSentSum, self.messagesReceivedSum);
            byte[] marshalledSummary = summary.setBytes();
            sender = new TCPSender(registrySocket);
            sender.sendData(marshalledSummary);
           // System.out.println("kust sent summary");
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    private void sendTaskComplete() throws IOException {
        TaskComplete message = new TaskComplete(InetAddress.getLocalHost().getHostAddress(), serverPort);
        byte[] marshalledMessage = message.setBytes();
        sender.sendData(marshalledMessage);
    }

    public synchronized void incrementReceivedStats(long payload) {
        messagesReceived+=1;
        messagesReceivedSum+=payload;
    }


}
