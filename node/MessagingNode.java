package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import util.InputHandler;

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
    private static InetAddress serverAddress;
    private static ArrayList<Connection> links = new ArrayList<>();

    private static InetAddress regInetAddress;
    private static int registryPort;
    private static Socket registrySocket;

    public static void main(String[] args) throws IOException {
        initiateServerSocket();
        

        regInetAddress = InetAddress.getByName(args[0]);
        registryPort = Integer.parseInt(args[1]);
        registrySocket = new Socket(regInetAddress, registryPort);

        InputHandler inputHanlder = new InputHandler();
        Thread inputHandlerThread = new Thread("Input handler Thread");
        inputHandlerThread.start();
        //create a new sender and receiver thread
        
        
        

    }

    private static void initiateServerSocket() { 
        boolean found = false;
        int initialPort = 8080;
        while(!found) {
            try {
                serverSocket = new ServerSocket(initialPort);
                found = true;
            } catch(IOException ioe) {
                ++initialPort;
            }
        }
    }

    
}
