package transport;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.net.Socket;
import java.net.SocketException;
import java.util.Arrays;

import wireformats.Protocol;
import wireformats.Protocol.messageType;
import wireformats.EventFactory;
import wireformats.Event;

public class TCPReceiverThread implements Runnable {

    private Socket socket;
    private DataInputStream din;

    public TCPReceiverThread(Socket socket) throws IOException {
        this.socket = socket;
        din = new DataInputStream(socket.getInputStream());
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
                event.getBytes(marshalledData); //itll take care of itself!
                
            } catch(SocketException se) {
                System.out.println(se.getMessage());
                break;
            } catch(IOException ioe) {
                System.out.println(ioe.getMessage());
                break;
            }
        }
        throw new UnsupportedOperationException("Unimplemented method 'run'");
    }

    public int readType(byte[] marshalledMessage) throws IOException {

        //in this we are going to read the type and return that shit son
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        int type = din.readInt();
        return type;
    }
    
}
