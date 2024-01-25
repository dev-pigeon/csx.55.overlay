package wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import node.MessagingNode;

public class Task_Initiate implements Event {

    int type = 5;
    int rounds = 0;


    public Task_Initiate() {
        this(0);
    }

    public Task_Initiate(int numRounds) {
        this.rounds = numRounds;
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
        // TODO Auto-generated method stub
        byte[] marshalledData = null;
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));
       
        dout.writeInt(type);
        dout.writeInt(rounds);

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
        rounds = din.readInt();

        bArrayInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner) {
        
            System.out.println("I am initiating task with " + rounds + " rounds");
            MessagingNode.initiateTask(rounds);
        
    }
    
}
