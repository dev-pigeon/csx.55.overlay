package wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

import node.MessagingNode;
import node.RegisteredNode;

public class RegisterResponse implements Event {

    int messageType = 1;
    byte statusCode;
    String info;

    public RegisterResponse() {
        this((byte) 0, null);
    }

    public RegisterResponse(byte status, String infoMessage) {
        this.statusCode = status;
        this.info = infoMessage;
    }

    @Override
    public int getType(byte[] marshalledMessage) throws IOException {
       //in this we are going to read the type and return that shit son
       ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);

       DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

       messageType = din.readInt();
       return messageType;
    }

    @Override
    public byte[] setBytes() throws IOException {
        // TODO Auto-generated method stub
        byte[] marshalledData = null;
        //make byre aray stream
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        //buffered ooutput that byearray output uses
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //write type
        dout.writeInt(messageType);
        //write the status code
        dout.writeByte(statusCode);
        //turn message into byte array, write its length, then write array
        byte[] messageBytes = info.getBytes();
        int infoLength = messageBytes.length;
        dout.writeInt(infoLength);
        dout.write(messageBytes);

        //flush the stream
        dout.flush();
        //set marshalleddata to outputstream
        marshalledData = baOutputStream.toByteArray();
        //close streams and return
        baOutputStream.close();
        dout.close();
        return marshalledData;
    }

    @Override
    public void getBytes(byte[] marhalledData) throws IOException {

        ByteArrayInputStream bArrayInputStream = new ByteArrayInputStream(marhalledData);
        DataInputStream din = new DataInputStream(new BufferedInputStream(bArrayInputStream));
        //read the type
        messageType = din.readInt();
        //the byte is marshalled first so read that
        statusCode = din.readByte();
        //get the length of the byte array string
        int messageLength  = din.readInt();
        //make a byte array for the message
        byte[] infoBytes = new byte[messageLength];
        //read them
        din.readFully(infoBytes);
        //set the info message
        info = new String(infoBytes);
        //close the streams
        bArrayInputStream.close();
        din.close();

    }
    @Override
    public void handleEvent(Object owner,RegisteredNode node) {
        //print the contents of the response to the screen
        System.out.println(info);
    }


}
