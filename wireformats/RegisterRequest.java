package wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;

import node.*;

/* note
 * this will only go to the REGISTRY
 */

public class RegisterRequest implements Event { 
    int type = 0;
    public String ipAddress;
    public int portNumber;
    
    public RegisterRequest() {
        this(null, 0);
    }

    public RegisterRequest(String address, int port) {
        this.ipAddress = address;
        this.portNumber = port;
    }

    public void getBytes(byte[] marshalledMessage) throws IOException {
        //this is the demarshalling!!!
        //System.out.println(" i am demarshalling the register request");
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);
        
        
        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();

        int IPLength = din.readInt();
    
        byte[] IPbytes = new byte[IPLength];
        din.readFully(IPbytes);
        //set string ip to this
        ipAddress = new String(IPbytes);

        portNumber = din.readInt();

        baInputStream.close();
        din.close();
    }

    @Override
    public int getType(byte[] marshalledMessage) throws IOException {
        //in this we are going to read the type and return that shit son
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = din.readInt();
        return type;
    }

    @Override
    public byte[] setBytes() throws IOException { //this is the marshalling for sending
        byte[] marhalledData = null;
        //create the byte array stream
        ByteArrayOutputStream baOutputStream = new ByteArrayOutputStream();
        //create the buffered output stream -> this is used to write the byes to marshalledData
        DataOutputStream dout = new DataOutputStream(new BufferedOutputStream(baOutputStream));

        //write the type first since that is the order wwe will demarshall
        dout.writeInt(type);
        //then write the IP
        byte[] ipBytes = ipAddress.getBytes();
        int elemLength = ipBytes.length;
        //System.out.println("writing ip length as = " + elemLength);
        dout.writeInt(elemLength);
        dout.write(ipBytes);
       
        //finally write the port number
        dout.writeInt(portNumber);
        dout.flush();
        marhalledData = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marhalledData;  
    }

    public int showType() {
        return type;
    }

    @Override
    public void handleEvent(Object owner) {
        //the owner of this is going to be the registered node since Req request will only be sent to these guys
        //therefor its safe to just cast that john
        //System.out.println("I am calling validate node with IP = " + ipAddress + " and portnumber = " + portNumber);
        Registry.ValidateNode((RegisteredNode)owner, ipAddress, portNumber);

    }    
}
