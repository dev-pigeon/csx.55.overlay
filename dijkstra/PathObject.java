package dijkstra;
import node.*;
public class PathObject {
    
    RegisteredNode from;
    RegisteredNode to;
    int weight;
    int localWeight;


    public PathObject(RegisteredNode fromNode, RegisteredNode toNode, int weight) {
        from = fromNode;
        to = toNode;
        this.weight = weight;
    }

    public void setLocalWeight(int weight) {
        System.out.println("my local weight is " + weight);
        this.localWeight = weight;
    }
}
