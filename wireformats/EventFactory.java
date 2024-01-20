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
            default:
                break;
        }
        return event;
    }
}
