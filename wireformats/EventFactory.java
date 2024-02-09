package csx55.overlay.wireformats;

import csx55.overlay.wireformats.*;

public class EventFactory {
    public static Event spawnEvent(int msgType) {
        Event event = null;
        switch (msgType) {
            case Protocol.Register_Request:
                
                event = new RegisterRequest();
                break;
            case Protocol.Register_Response:
                event = new RegisterResponse();
                break;
            case Protocol.Messaging_Nodes_List:
                event = new Messaging_Nodes_List();
                break;
            case Protocol.Task_Initiate:
                event = new Task_Initiate();
                break;
            case Protocol.Message:
                event = new Message();
                break;
            case Protocol.Deregister_Request:
                event = new DeregisterRequest();
                break;
            case Protocol.Pull_Traffic_Summary:
                event = new PullTrafficSummary();
                break;
            case Protocol.Traffic_Summary:
                event = new TrafficSummary();
                break;
            case Protocol.Task_Complete:
                event = new TaskComplete();
                break;
            case Protocol.Link_Weights:
                event = new Link_Weights();
                break;
            case Protocol.PeerPortNumber:
                event = new PeerPortNumber();
                break;
            default:
                break;
        }
        return event;
    }
}
