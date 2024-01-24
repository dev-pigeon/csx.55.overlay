package wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import node.MessagingNode;

public class Messaging_Nodes_List implements Event {

    int type = 3;
    int numOfPeerNodes;
    //I will generate the node info list as a ArrayList<String> but just write the strings individually
    static ArrayList<String> peerNodeList = new ArrayList<>();
    public Messaging_Nodes_List() {
        this(new ArrayList<String>(), 0);
    }

    public Messaging_Nodes_List(ArrayList<String> peerNodeList, int numberOfConnections) {
        this.peerNodeList = peerNodeList;
        this.numOfPeerNodes = numberOfConnections;
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

        //write the type
        dout.writeInt(type);
        //write the number of peer nodes
        dout.writeInt(numOfPeerNodes);
        
        //write the stuffs
        for(int i = 0; i < numOfPeerNodes; ++i) {
            byte[] nodeInfoBytes = peerNodeList.get(i).getBytes();
            int infoLength = nodeInfoBytes.length;
            dout.writeInt(infoLength);
            dout.write(nodeInfoBytes);
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

        //read the type
        type = din.readInt();
        //read the number of nodes
        numOfPeerNodes = din.readInt();
        System.out.println("number of peer nodes = " + numOfPeerNodes);
        //read each of the node information and put into an array!
        for(int i = 0; i < numOfPeerNodes; ++i) {
            int infoLength = din.readInt();
            byte[] infoBytes = new byte[infoLength];
            din.readFully(infoBytes);
            //for some reason there is a null pointer type deal here
            String nodeInfo = new String(infoBytes).trim();
            System.out.println("peer node info = " + nodeInfo);
            //if list is null then instantiate it first in this method?
            peerNodeList.add(nodeInfo);
        }

        bArrayInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner) {
        if(owner instanceof MessagingNode) {
            try {
                ((MessagingNode)owner).addConnectionProtocol(peerNodeList);
            } catch(IOException ioe) {
                System.out.println(ioe.getMessage());
            }
        }
    }

    
    
}
