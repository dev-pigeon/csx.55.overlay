package wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import node.RegisteredNode;
import node.Registry;

public class TaskComplete implements Event{
    int type = 6;

    String ipAddress;
    int port;
    
    public TaskComplete() {
        this(null, 0);
    }

    public TaskComplete(String ipString, int port) {
        this.ipAddress = ipString;
        this.port = port;
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
        byte[] marhalledData = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);

        byte[] ipBytes = ipAddress.getBytes();
        int elemLength = ipBytes.length;
        dout.writeInt(elemLength);
        dout.write(ipBytes);

        dout.writeInt(port);

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

        int elemLength = din.readInt();
        byte[] ipBytes = new byte[elemLength];
        din.readFully(ipBytes);
        ipAddress = new String(ipBytes);

        port = din.readInt();

        baInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner,RegisteredNode node) {
        ((Registry)owner).checkTaskComplete();
    }
}

