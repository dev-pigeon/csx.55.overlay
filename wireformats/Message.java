package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import csx55.overlay.node.MessagingNode;
import csx55.overlay.node.RegisteredNode;

public class Message implements Event{

    int type = 9;
    int payload = 0;
    String route;

    public Message() {
        this(0, null);
    }

    public Message(int payload, String route) {
        this.payload = payload;
        this.route = route;
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
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.writeInt(payload);

        dout.flush();

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
        payload = din.readInt();

        bArrayInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner, RegisteredNode node) {
        if(owner instanceof MessagingNode) {
              ((MessagingNode)owner).incrementReceivedStats(payload);
        } 
    }
    
}
