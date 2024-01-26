package wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import node.MessagingNode;

public class PullTrafficSummary implements Event {
    int type = 7;

    @Override
    public int getType(byte[] marshalledMessage) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));
        type = din.readInt();
        return type;
    }

    @Override
    public byte[] setBytes() throws IOException {
        byte[] marhalledData = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);
        dout.flush();
        marhalledData = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marhalledData;

        }

    @Override
    public void getBytes(byte[] marhalledData) throws IOException {
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marhalledData);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();

        baInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner) { 
       MessagingNode.sendTrafficSummary();
    }
}
