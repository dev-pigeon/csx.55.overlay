package wireformats;

import wireformats.Protocol.messageType;

public class EventFactory {
    public static Event spawnEvent(messageType msgType) {
        Event event = null;
        switch (msgType) {
            case Register_Request:
                
                event = new RegisterRequest();
                break;
            case Register_Response:
                event = new RegisterResponse();
                break;
            case Messaging_Nodes_List:
                event = new Messaging_Nodes_List();
                break;
            case Task_Initiate:
                event = new Task_Initiate();
                break;
            case Message:
                event = new Message();
                break;
            default:
                break;
        }
        return event;
    }
}
