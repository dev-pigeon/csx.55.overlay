package util;

import java.util.ArrayList;
import java.util.Map;

import node.*;

public class OverlayCreator {
    
    ArrayList<RegisteredNode> masterList;
    int connectionRequirement;

    RegisteredNode candidat1;
    RegisteredNode candidat2;

    public OverlayCreator(ArrayList<RegisteredNode> nodes, int connectionRequirement) {
        this.masterList = nodes;
        this.connectionRequirement = connectionRequirement;
    }


    public void buildOverlay() {
        formLinearTopology(); //tested function that works
        printAllSizes();
        boolean done = false;
        int maxCR = masterList.get(0).peerNodes.size() + 1;
        System.out.println(meetRequirements(masterList.get(0).peerNodes.size()));
        while(!done) {
           
            if(!meetRequirements(maxCR)) {
                //make requirement
            }
            findCandidates();

            if(candidat1 == null) {
                done = true;
            } else {
                
                if(masterList.indexOf(candidat2) == (masterList.indexOf(candidat1) + 1)) {
                    System.out.println("our fun condition is reached");
                    System.exit(0);
                }
                
                
                formConnection(candidat1, candidat2, 0);
                
                printAllSizes();
                
            }
        
        candidat1 = null;
        candidat2 = null;

            
            
        }
        //System.out.println(hasPartitions());
    }

    private void formLinearTopology() {
        //take the masterList and link all the nodes together like a doubly linked list
        /* for a connection to be bilatoral using the peerNode hashmaps if
        A is in B's map then B must be in A's map
         */ 

        for(int i = 0; i < masterList.size()-1; ++i) {
            //check if i == size() - 1
            formConnection(masterList.get(i), masterList.get(i+1), 0);
        }

        formConnection(masterList.get(0), masterList.get(masterList.size()-1), 0);
    }
    
    private void formConnection(RegisteredNode nodeA, RegisteredNode nodeB, int weight) {
        if(nodeA == null) {
            System.out.println("Error:Node A of the nodes is null!");
            System.exit(0);
        }
        if ( nodeB == null) {
            System.out.println("Error: Node B of the nodes is null!");
            System.exit(0);
        }
        
      
        if (nodeA.peerNodes.get(nodeB) == null) {
            nodeA.peerNodes.put(nodeB, weight);
        }
       
    
        
        if (nodeB.peerNodes.get(nodeA) == null) {
            nodeB.peerNodes.put(nodeA, weight);
        }
   

      
    }
    

    public boolean hasPartitions() {
        //call DFS
        ArrayList<RegisteredNode> visistedNodes = new ArrayList<>();
        DFS(masterList.get(0), visistedNodes);
        
        System.out.println();
        
        return masterList.equals(visistedNodes);
    }

    private void DFS(RegisteredNode current, ArrayList<RegisteredNode> visitedNodes) {
        visitedNodes.add(current);

        for(RegisteredNode peerNode : current.peerNodes.keySet()) {
            if(!visitedNodes.contains(peerNode)) {
                DFS(peerNode, visitedNodes);
            }
        }
    }


    private void findCandidates() {
        boolean foundFirst = false;
        for (int i = 0; i < masterList.size(); ++i) {
            if (masterList.get(i).peerNodes.size() < connectionRequirement) {
                //System.out.println("candidate " + i + " has " + masterList.get(i).peerNodes.size());
                if (!foundFirst) {
                    candidat1 = masterList.get(i);
                    foundFirst = true;
                } else {
                    if (candidat1 != null && candidat1.peerNodes.get(masterList.get(i)) == null && masterList.get(i).peerNodes.size() < connectionRequirement) {
                        candidat2 = masterList.get(i);
                    }
                }
            }
        }

        if(candidat2 == null && candidat1 != null) {
            //if we are here it means that the last two are neighbors
            //we need to set candidat two are neighbors, which means we need to find a new candidat2
            //we do this by 
            printAllSizes();
            System.out.println("condition reached");
            System.out.println("searching for alternate");
            alternateCandidat2();
        }


        if(foundFirst == false) return;
    
    }
    
    private void alternateCandidat2() {
        RegisteredNode neighborOfCandidat1 = masterList.get(masterList.indexOf(candidat1) + 1);
        candidat2 = masterList.get(masterList.size()-1);
        //we have candidat one is this case
        //we need to find a node in candidat2 that neighborOf1 is not connected to
        RegisteredNode candidat2Neighbor = null;
        for (Map.Entry<RegisteredNode, Integer> entry : candidat2.peerNodes.entrySet()) {
            RegisteredNode peerNode = entry.getKey();
            if(neighborOfCandidat1.peerNodes.get(peerNode) == null) {
                candidat2Neighbor = peerNode;
                break;
            }

            
        }

            System.out.println("found a neighbor of candidat2");
            System.out.println("candidat 1 is at " + masterList.indexOf(candidat1));
            System.out.println("candiat 2 is at " + masterList.indexOf(candidat2));


            
            
           removePeer(candidat2, candidat2Neighbor);
           printAllSizes();
        
          
           
           
        }
        
    

    private void removePeer(RegisteredNode node1, RegisteredNode node2) {
        node1.peerNodes.remove(node2);
        node2.peerNodes.remove(node1);
    }

    public boolean meetRequirements(int requirement) {
       
        for(int i = 0; i < masterList.size(); ++i) {
            if(masterList.get(i).peerNodes.size() != requirement) {
                //System.out.println("position " + i + " does not meet CR and has " + masterList.get(i).peerNodes.size() + " connections");
                return false;
            }
        }
        return true;
    }

    private void printAllSizes() {
        for(int i = 0; i < masterList.size(); i++) {
            System.out.print(masterList.get(i).peerNodes.size());
        } 
        
     
        System.out.println();
    }
}
