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
            case Deregister_Request:
                event = new DeregisterRequest();
                break;
            case Pull_Traffic_Summary:
                event = new PullTrafficSummary();
                break;
            case Traffic_Summary:
                event = new TrafficSummary();
                break;
            case Task_Complete:
                event = new TaskComplete();
                break;
            case Link_Weights:
                event = new Link_Weights();
                break;
            default:
                break;
        }
        return event;
    }
}
