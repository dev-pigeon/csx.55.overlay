package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.UnknownHostException;

public class Registry {
    //the values for port number and server address are passed from command line
    private static int port;
    private static InetAddress serverAddress;
    private static ServerSocket serverSocket;

    public static void main(String[] args) {
        checkArgs(args);

        String addressString = args[0];
        setServerAddress(addressString);

        port = Integer.parseInt(args[1]);
        initializeServerSocket(port);
    }

    private static void checkArgs(String[] args) {
        if(args.length != 2) {
            System.out.println("You must specify both a server address and port in that order. Try again.");
            System.exit(0);
        }
    }

    private static void setServerAddress(String addressString) {
        try {
            serverAddress = InetAddress.getByName(addressString);
        } catch(UnknownHostException uhe) {
            System.out.println(uhe.getMessage());
        }
    }

    private static void initializeServerSocket(int port) {
        try {
            serverSocket = new ServerSocket(port);
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }
}