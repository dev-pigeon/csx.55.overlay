package csx55.overlay.dijkstra;


import csx55.overlay.node.*;
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
        this.localWeight = weight;
    }
}
