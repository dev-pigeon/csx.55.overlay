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
import csx55.overlay.node.Registry;

public class DeregisterRequest implements Event {
    int type = 2;
    String ipAddress;
    int port;

    public DeregisterRequest() {
        this(null, 0);
    }

    public DeregisterRequest(String address, int port) {
        this.ipAddress = address;
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
        byte[] ipbytes = ipAddress.getBytes();
        int elemLength = ipbytes.length;
        dout.writeInt(elemLength);
        dout.write(ipbytes);
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
    public void handleEvent(Object owner, RegisteredNode node) {
        //this is always gonna be the registry but I like "defensive" programming
       //only a registered node will receive these
       
       if(owner instanceof Registry) {
         ((Registry)owner).checkDeregisterRequest(ipAddress, port, node);
       } else if(owner instanceof MessagingNode) {
            if(port == 0) {
               // System.out.println(ipAddress);
            } else {
                //
            }
       }
    }

}
