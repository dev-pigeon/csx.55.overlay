package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import util.*;
import transport.*;



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
  
    protected static ArrayList<RegisteredNode> registeredNodes = new ArrayList<>();
    public static void main(String[] args) throws IOException, InterruptedException {
       
        port = Integer.parseInt(args[0]);

        initiateServerSocket();
        serverAddress = InetAddress.getLocalHost();
        System.out.println("port: " + serverSocket.getLocalPort() + " address: " + serverAddress.getHostAddress());

       
        InputHandler inputHanlder = new InputHandler();
        Thread inputHandlerThread = new Thread(inputHanlder,"Input handler Thread");
        inputHandlerThread.start();

        
        TCPServerThread serverListener = new TCPServerThread(serverSocket, registeredNodes, "Registry");
        Thread tcpServerThread = new Thread(serverListener,"TCPServerThread");
        tcpServerThread.start();

        inputHandlerThread.join();
        tcpServerThread.join();
        
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

   
    
}