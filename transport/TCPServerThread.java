package transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServerThread implements Runnable {
    //this will be spawned by both entities and will simply be the listener of connections (not sure how itll work like addding things lmao)
    int serverPort;
    ServerSocket serverSocket;
    ArrayList<Socket> socketList = new ArrayList<>();

    public TCPServerThread(int portNumber, ArrayList<Socket> socketList) {
        this.serverPort = portNumber;
        this.socketList = socketList;
        initializeServerSocket();
    }

    private void initializeServerSocket() {
        try {
            serverSocket = new ServerSocket(serverPort);
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    @Override
    public void run() {
        while(true) {
           Socket socket = acceptConnections();
           socketList.add(socket);
        }
    }

    private Socket acceptConnections() {
        Socket socket = null;
        try {
            socket = serverSocket.accept();
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return socket;
    }
}
