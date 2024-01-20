package transport;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

public class TCPServerThread implements Runnable {
    //this will be spawned by both entities and will simply be the listener of connections (not sure how itll work like addding things lmao)
    
    ServerSocket serverSocket;
    ArrayList<Socket> socketList = new ArrayList<>();

    public TCPServerThread(ServerSocket serverSocket, ArrayList<Socket> socketList) {
        this.serverSocket = serverSocket;
        this.socketList = socketList;
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
            System.out.println("a connection has been accepted!");
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
        return socket;
    }
}
