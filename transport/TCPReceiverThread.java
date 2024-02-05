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
   
    private Object owner;
    private RegisteredNode node;
    //it will eventually change to the object for dikjstra


    public TCPReceiverThread(Socket socket, Object owner, RegisteredNode node) throws IOException {
        this.socket = socket;
        din = new DataInputStream(new BufferedInputStream(socket.getInputStream()));       
        this.owner = owner;
        this.node = node;
    }

    @Override
    public void run() {
        int dataLength;
        while(socket!= null) {
            try {
                //read the length so we know how big the array is
                dataLength = din.readInt();
                byte[] marshalledData = new byte[dataLength];
                din.readFully(marshalledData, 0, dataLength); //read the entire thing without being interupted
                
                byte[] marshallCopy = Arrays.copyOf(marshalledData, marshalledData.length);
                int type = readType(marshallCopy);               
                
                messageType msgType = Protocol.getMessageType(type);
               
                Event event = EventFactory.spawnEvent(msgType); 
                //get the bytes and stuff, the event will handle the rest                
                event.getBytes(marshalledData);                 
                //call handle event based on the type that it is
                event.handleEvent(owner, node); //diff events will call methods based on input of owner (change owner to a literal instance of registry or msg node)
               
            } catch(SocketException se) {
                System.out.println(se.getMessage());
                break;
            } catch(IOException ioe) {
                System.out.println(ioe.getMessage());
                break;
            }
        } 
        System.out.println("SOCKET WAS NULL?");
    }

    public static int readType(byte[] marshalledMessage) throws IOException {

        //in this we are going to read the type and return that shit son
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        int type = din.readInt();
        return type;
    }
    
}
