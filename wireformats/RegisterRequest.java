package wireformats;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/* note
 * this will only go to the REGISTRY
 */

public class RegisterRequest implements Event { 
    int type = 0;
    String ipAddress;
    int portNumber;
    
    public RegisterRequest() {
        this(null, 0);
    }

    public RegisterRequest(String address, int port) {
        this.ipAddress = address;
        this.portNumber = port;
    }

    public void getBytes(byte[] marshalledMessage) throws IOException {
        //this is the demarshalling!!!
        ByteArrayInputStream baInputStream = new ByteArrayInputStream(marshalledMessage);

        DataInputStream din = new DataInputStream(new BufferedInputStream(baInputStream));

        type = getType(marshalledMessage);

        int IPLength = din.readInt();
        byte[] IPbytes = new byte[IPLength];
        din.readFully(IPbytes);

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
        writeIntField(dout, type);
        //then write the IP
        writeIP(dout);
        //finally write the port number
        writeIntField(dout, portNumber);
        dout.flush();
        marhalledData = baOutputStream.toByteArray();

        baOutputStream.close();
        dout.close();
        return marhalledData;  
    }

    private void writeIntField(DataOutputStream dout, int field) {
        try {
            dout.writeInt(field);
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    private void writeIP(DataOutputStream dout) {
        try {
            byte[] ipBytes = ipAddress.getBytes();
            int elemLength = ipBytes.length;
            dout.writeInt(elemLength);
            dout.write(ipBytes);
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
    }

    public int showType() {
        return type;
    }

    
}
