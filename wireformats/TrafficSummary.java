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

public class TrafficSummary implements Event {
    int type = 8;
    int numSent;
    long sumSent;
    int numReceived;
    long sumReceived;
    //int numRelayed;
    //long sumRelayed;

    public TrafficSummary() {
        this(0,0,0,0);
    }

    public TrafficSummary(int numSent, int numReceived, /*int numRelayed,*/ long sumSent, long sumReceived /*, long sumRelayed*/ ) {
        this.numSent = numSent;
        this.numReceived = numReceived;
        this.sumSent = sumSent;
        this.sumReceived = sumReceived;
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
       
        //we are going to write in the order of the constructor
        byte[] marshalledData = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));


        dout.writeInt(type);

        dout.writeInt(numSent);
        dout.writeInt(numReceived);
        //dout.writeInt(numRelayed);
        dout.writeLong(sumSent);
        dout.writeLong(sumReceived);
        //dout.writeLong(sumRelayed);
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

        numSent = din.readInt();
        numReceived = din.readInt();
        //numRelayed = din.readInt();

        sumSent = din.readLong();
        sumReceived = din.readLong();
        //dout.sumRelayed = din.readLong();

        bArrayInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner,RegisteredNode node) {        
        ((Registry)owner).storeTrafficSummary(numSent, numReceived, sumSent, sumReceived);
    }

}
