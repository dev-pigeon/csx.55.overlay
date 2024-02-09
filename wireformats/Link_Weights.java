package csx55.overlay.wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;

import csx55.overlay.node.*;


public class Link_Weights implements Event {
    
    ArrayList<String> linkWeightMessages;
    int type = 4;
    int numLeftOffset = 1;
    
    public Link_Weights() {
        this(new ArrayList<>());
    }

    public Link_Weights(ArrayList<String> messages) {
        this.linkWeightMessages = messages;
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
        //just need to write the number of messages and then each message in the list
        //the parsing is done on the receiving end
        byte[] marshalledData = null;
        //make byre aray stream
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        //buffered ooutput that byearray output uses
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        dout.writeInt(type);

        int numberOfMessages = linkWeightMessages.size();
        dout.writeInt(numberOfMessages);

        for(int i = 0; i < numberOfMessages; ++i) {
            byte[] messageBytes = linkWeightMessages.get(i).getBytes();
            int elementLength = messageBytes.length;
            dout.writeInt(elementLength);
            dout.write(messageBytes);
        }

        //flush stream
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

        int numberOfMessages = din.readInt();
        for(int i = 0; i < numberOfMessages; ++i) {
            int elementLength = din.readInt();
            byte[] messageBytes = new byte[elementLength];
            din.readFully(messageBytes);
            String message = new String(messageBytes);
            linkWeightMessages.add(message);
        }

        bArrayInputStream.close();
        din.close();
    }


    

    @Override
    public void handleEvent(Object owner, RegisteredNode node) {
        for(int i = 0; i < linkWeightMessages.size(); ++i) {
            parseLinkMessage(owner,linkWeightMessages.get(i));
        }
    }

    private void parseLinkMessage(Object owner, String message) {
        String nodeOneString = message.substring(0, message.indexOf(" "));
        String nodeTwoString = message.substring(message.indexOf(" ") + 1, message.indexOf("-"));
        int weight = Integer.parseInt(message.substring(message.indexOf("-") + 1, message.length()));

        String nodeOneIP = parseAddress(nodeOneString);
        String nodeTwoIP = parseAddress(nodeTwoString);

        int nodeOnePort = parseIP(nodeOneString);
        int nodeTwoPort = parseIP(nodeTwoString);

        RegisteredNode nodeOne = new RegisteredNode(null, nodeOneIP, nodeOnePort);
        RegisteredNode nodeTwo = new RegisteredNode(null, nodeTwoIP, nodeTwoPort);
        
        try {
            ((MessagingNode)owner).linkWeightProtocol(nodeOne, nodeTwo, weight, linkWeightMessages.size() - numLeftOffset);
            numLeftOffset++;
        } catch (UnknownHostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
    }

    private String parseAddress(String message) {
        String address = message.substring(0, message.indexOf(":"));
        return address;
    }

    private int parseIP(String message) {
        int port = Integer.parseInt(message.substring(message.indexOf(":") + 1, message.length()));
        return port;
    }
}
