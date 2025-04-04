package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import csx55.overlay.node.RegisteredNode;

public class PeerPortNumber implements Event {

    int type = 11;
    int portNumber = 0;

    public PeerPortNumber() {
        this(0);
    }

    public PeerPortNumber(int port) {
        this.portNumber = port;
    } 

    @Override
    public int getType(byte[] marshalledMessage) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        return type;
    }

    @Override
    public byte[] setBytes() throws IOException {
        byte[] marshalledData = null;
        //make byre aray stream
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        //buffered ooutput that byearray output uses
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(portNumber);

        dout.flush();
        //set stream to byte array
        marshalledData = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marshalledData;
    }

    @Override
    public void getBytes(byte[] marhalledData) throws IOException {
        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(marhalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));
        type = din.readInt();
        portNumber = din.readInt();
        bArrayInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner, RegisteredNode node) {
        node.setPortNum(portNumber);
    }
    
}
