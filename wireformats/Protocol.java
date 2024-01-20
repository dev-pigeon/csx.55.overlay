package wireformats;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.DataInput;
import java.io.DataInputStream;
import java.io.IOException;

/* Notes
 * The protocol calss is a helper class to the TCPReceiver, whenever a msg is received, you get the type with this?
 * after which the receiver checks what type it is and then demarshalls it (which will be in the wireformat class for whatever it actually is)
 */

public class Protocol  {
    
    static final int Register_Request = 0;
    static final int Register_Response = 1;
    static final int Deregister_Request = 2;
    static final int Messaging_Nodes_List = 3;
    static final int Link_Weights = 4;
    static final int Task_Initiate = 5;
    static final int Task_Complete = 6;
    static final int Pull_Traffic_Summary = 7;
    static final int Traffic_Summary = 8;

    
    public static enum messageType {
        Register_Request,
        Register_Response,
        Deregister_Request,
        Messaging_Nodes_List,
        Link_Weights,
        Task_Initiate,
        Task_Complete,
        Pull_Traffic_Summary,
        Traffic_Summary
    }

   public static messageType getMessageType(int typeNum) {
        switch (typeNum) {
            case Register_Request:
                return messageType.Register_Request;
            case Register_Response:
                return messageType.Register_Response;
            case Deregister_Request:
                return messageType.Deregister_Request;
            case Messaging_Nodes_List:
                return messageType.Messaging_Nodes_List;
            case Link_Weights:
                return messageType.Link_Weights;
            case Task_Initiate:
                return messageType.Task_Initiate;
            case Task_Complete:
                return messageType.Task_Complete;
            case Pull_Traffic_Summary:
                return messageType.Pull_Traffic_Summary;
            default:
                return messageType.Traffic_Summary;
        }
    }
}

/* receiver will get the type numba
 * throw that int into factory
 * spwans thing 
 * then you do what need with certain event / wireformat
 */
