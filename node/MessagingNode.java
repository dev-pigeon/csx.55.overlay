package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.ArrayList;

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

    public static void main(String[] args) {
        initiateServerSocket();
        serverAddress = serverSocket.getInetAddress();
        //start a sender thread
        //start a receiver thread
        //request registry
    }

    private static void initiateServerSocket() { //tested and works for both cases
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
