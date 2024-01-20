package node;

import java.net.InetAddress;

public class Connection {
    
    private static InetAddress nodeConnectedTo;
    private static int edgeWeight;

    public Connection(InetAddress nodeConnectedTo, int edgeWeight) {
        this.nodeConnectedTo = nodeConnectedTo;
        this.edgeWeight = edgeWeight;
    }

    public void upDateEdgeWeight(int newWeight) {
        this.edgeWeight = newWeight;
    }
}
