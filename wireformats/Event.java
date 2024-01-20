package wireformats;

import java.io.IOException;

public interface Event {
    public int getType(byte[] marshalledMessage) throws IOException; //determines the type based on the messageType integer
    public byte[] setBytes() throws IOException; //get bytres is the marshalling technique and will be used for SENDING
    public void getBytes(byte[] marhalledData) throws IOException; //this demarshalls the message and populates the wireformat fields

}
