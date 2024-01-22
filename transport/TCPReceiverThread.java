package transport;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Inet6Address;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;

import wireformats.*;

import wireformats.Protocol.messageType;

import node.*;

public class TCPReceiverThread implements Runnable {

    private Socket socket;
    private DataInputStream din;
    private ArrayList<RegisteredNode> registeredNodes;
    private String owner;//this is my like denothing thing for now
    //it will eventually change to the object for dikjstra
    private String originIP;

    public TCPReceiverThread(Socket socket, String owner) throws IOException {
        this.socket = socket;
        din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        originIP = socket.getInetAddress().getHostAddress();
        this.registeredNodes = registeredNodes;
        this.owner = owner;
    }

    @Override
    public void run() {
        int dataLength;
        System.out.println("TCP receiver thread has been started");
        while(socket!= null) {
            try {
                //read the length so we know how big the array is
                dataLength = din.readInt();
                byte[] marshalledData = new byte[dataLength];
                din.readFully(marshalledData, 0, dataLength); //read the entire thing without being interupted
                
                byte[] marshallCopy = Arrays.copyOf(marshalledData, marshalledData.length);
                int type = readType(marshallCopy);

                
                
                messageType msgType = Protocol.getMessageType(type);

                

                Event event = EventFactory.spawnEvent(msgType); //may need to pass the socket into the event so you can like, send stuff back
                
                //get the bytes and stuff, the event will handle the rest
                System.out.println("THE MARSHALLED DATA = " + marshalledData);
                if(event == null) {
                    System.out.println("WHAT");
                }
                event.getBytes(marshalledData); 
                //call handle event based on the type that it is
                event.handleEvent(owner); //diff events will call methods based on input of owner (change owner to a literal instance of registry or msg node)
                
            } catch(SocketException se) {
                System.out.println(se.getMessage());
                break;
            } catch(IOException ioe) {
                System.out.println(ioe.getMessage());
                break;
            }
        }
    }

    public static int readType(byte[] marshalledMessage) throws IOException {

        //in this we are going to read the type and return that shit son
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        int type = din.readInt();
        return type;
    }
    
}
