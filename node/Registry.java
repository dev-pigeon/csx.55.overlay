package node;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

import util.*;
import transport.*;

public class Registry {
    //the values for port number and server address are passed from command line
    private static int port;
    private static InetAddress serverAddress;
    private static ServerSocket serverSocket;
    private static ArrayList<Socket> socketList = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
       
        port = Integer.parseInt(args[0]);

        initiateServerSocket();
        serverAddress = serverSocket.getInetAddress();
        System.out.println("port: " + serverSocket.getLocalPort() + " address: " + serverAddress);

       
        InputHandler inputHanlder = new InputHandler();
        Thread inputHandlerThread = new Thread(inputHanlder,"Input handler Thread");
        inputHandlerThread.start();

        
        TCPServerThread serverListener = new TCPServerThread(port, socketList);
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