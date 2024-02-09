package csx55.overlay.wireformats;

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
    static final int Message = 9;
    static final int PeerPortNumber = 11;

    
    

   public static int getMessageType(int typeNum) {
        switch (typeNum) {
            case Register_Request:
                return Register_Request;
            case Register_Response:
                return Register_Response;
            case Deregister_Request:
                return Deregister_Request;
            case Messaging_Nodes_List:
                return Messaging_Nodes_List;
            case Link_Weights:
                return Link_Weights;
            case Task_Initiate:
                return Task_Initiate;
            case Task_Complete:
                return Task_Complete;
            case Pull_Traffic_Summary:
                return Pull_Traffic_Summary;
            case Message:
                return Message;
            case PeerPortNumber:
                return PeerPortNumber;
            default:
                return Traffic_Summary;
        }
    }
}

/* receiver will get the type numba
 * throw that int into factory
 * spwans thing 
 * then you do what need with certain event / wireformat
 */
