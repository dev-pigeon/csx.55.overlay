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
import node.RegisteredNode;

public class Messaging_Nodes_List implements Event {

    int type = 3;
    int numOfPeerNodes;
    //I will generate the node info list as a ArrayList<String> but just write the strings individually
   
    ArrayList<String> connectionList = new ArrayList<String>();

    /*
     * this is going to change to just be one ArrayList<String> called connection list
     */

    public Messaging_Nodes_List() {
        this(new ArrayList<String>(),0);
    }

    public Messaging_Nodes_List(ArrayList<String> connectionList, int numberOfConnections) {
        this.connectionList = connectionList;
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

        /* since this is going to be unpacked as two lists
         * one for IP's and one for corresponding port numbers
         * after we write num connections...
         * we will write in order of, (IP LENGTH, IP, PORT) 
         * that pattern will repeat for num connections
         */
        
        //write the type
        dout.writeInt(type);
        //write the number of peer nodes
        dout.writeInt(numOfPeerNodes);
        
        //write the stuffs
        for(int i = 0; i < numOfPeerNodes; ++i) {
           //turn each index of connectionList into a byte array
           String temp = connectionList.get(i);
           byte[] connectionBytes = temp.getBytes();
           int elementLengh = connectionBytes.length;

           dout.writeInt(elementLengh);
           dout.write(connectionBytes); 
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
       // System.out.println("number of peer nodes = " + numOfPeerNodes);
        //read each of the node information and put into an array!
        for(int i = 0; i < numOfPeerNodes; ++i) {
           //just read in all of the items 
           int elementLengh = din.readInt();
           byte[] messageBytes = new byte[elementLengh];
           din.readFully(messageBytes);
           String message = new String(messageBytes);
           connectionList.add(message);
        }

        bArrayInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner, RegisteredNode node)  {
        try {
            ((MessagingNode)owner).addConnectionProtocol(connectionList);
        } catch (IOException ioe) {
            System.out.println(ioe.getMessage());
        }
           
    }

    
    
}
