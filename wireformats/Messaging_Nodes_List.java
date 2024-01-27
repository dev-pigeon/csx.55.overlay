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
    ArrayList<String> connectionIPList = new ArrayList<>();
    ArrayList<Integer> connectionPortList = new ArrayList<>();

    public Messaging_Nodes_List() {
        this(new ArrayList<String>(), new ArrayList<Integer>(), 0);
    }

    public Messaging_Nodes_List(ArrayList<String> connectionIPList, ArrayList<Integer> connectionPortList, int numberOfConnections) {
        this.connectionIPList = connectionIPList;
        this.connectionPortList = connectionPortList;
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
            byte[] ipBytes = connectionIPList.get(i).getBytes();
            int ipLength = ipBytes.length;
            dout.writeInt(ipLength);
            dout.write(ipBytes);
            int port = connectionPortList.get(i);
           // System.out.println("writing port");
            dout.writeInt(port);
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
            
            int ipLength = din.readInt();
           // System.out.println("IP length = " + ipLength);
            byte[] ipBytes = new byte[ipLength];
            
            din.readFully(ipBytes);
            String connectionIP = new String(ipBytes);
            //System.out.println("IP = " + connectionIP);
            
            //System.out.println("reading port");
            int port = din.readInt();
           
            connectionIPList.add(connectionIP);
            connectionPortList.add(port);
           // System.out.println("finished");
        }

        bArrayInputStream.close();
        din.close();
    }

    @Override
    public void handleEvent(Object owner, RegisteredNode node)  {
        try {
            ((MessagingNode)owner).addConnectionProtocol(connectionIPList, connectionPortList);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
           
    }

    
    
}
