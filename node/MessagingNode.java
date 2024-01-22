package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import transport.TCPReceiverThread;
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
    private static InetAddress serverAddress;

    private static InetAddress regInetAddress;
    private static int registryPort;
    private static Socket registrySocket;

    private static TCPSender sender;

    public static ArrayList<RegisteredNode> registeredNodes;

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

        sendRegisterRequest();

        RegisteredNode registryConnectionNode = new RegisteredNode(registrySocket,"MessagingNode"); //this node is what you receive form registry on!
    
        TCPServerThread server = new TCPServerThread(serverSocket, registeredNodes, "MessagingNode");
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
        RegisterRequest registerRequest = new RegisterRequest(InetAddress.getLocalHost().getHostAddress(), serverPort);
        byte[] registeryMessage = registerRequest.setBytes();
        sender.sendData(registeryMessage);
    }  
}
