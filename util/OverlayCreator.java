package util;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Map;
import java.util.Random;

import node.*;

public class OverlayCreator {
    
    ArrayList<RegisteredNode> masterList;
    ArrayList<String> linkMessages;
    int connectionRequirement;

    int min = 2; //form linear topology ensures everyone starts with 2 nodes
    int max = 3; //max is always ONLY one more than min
    Random rand = new Random(); //used to find random nodes 

    public OverlayCreator(ArrayList<RegisteredNode> nodes,ArrayList<String> linkMessages, int connectionRequirement) {
        this.masterList = nodes;
        this.connectionRequirement = connectionRequirement;
        this.linkMessages = linkMessages;
    }


    public void buildOverlay() {

        if(masterList.size() <= connectionRequirement || ((masterList.size() * connectionRequirement) % 2 != 0)) {
            System.out.println("ERROR: Impossible to generate overlay with given inputs");
            return;
        }
        formLinearTopology(); //tested function that works
      
        while(max <= connectionRequirement) {
            RegisteredNode candidatOne = findCandidatOne();
            RegisteredNode candidatTwo = findCandidate2(candidatOne);
           
            //we need a method to find a random candidat2 given that its not candidat1
            //check if it returns null, if so we need to adjust the graph
            if(candidatTwo == null) {
                if(countUnderFundedNodes() == 1) {           
                    RegisteredNode fundedNode = findFundedNode(candidatOne);
                    formConnection(candidatOne, fundedNode, 0);
                } else {
                    RegisteredNode underFundedNode = findUnderFundedNode(candidatOne);
                    adjustOverlay(candidatOne, underFundedNode);
                }
                
            } else {
                formConnection(candidatOne, candidatTwo, 0);
            }
            checkForMax();
        }
    }

    /* formLinearTopology()
     * takes the master list and loops over it and makes a bilateral connection 
     * at each index. After the loop it connects the front to the back to ensure every node
     * has 2 connections to start
     */
    private void formLinearTopology() {
        for(int i = 0; i < masterList.size()-1; ++i) {
            formConnection(masterList.get(i), masterList.get(i+1), 0);
        }

        formConnection(masterList.get(0), masterList.get(masterList.size()-1), 0);
    }

    private RegisteredNode findFundedNode(RegisteredNode candidat1) {
        RegisteredNode funded = null;
        for(int i = 0; i < masterList.size(); ++i) {
            if((candidat1.peerNodes.containsKey(masterList.get(i)) == false) && (masterList.get(i).peerNodes.containsKey(candidat1) == false) && masterList.get(i) != candidat1) {
                funded = masterList.get(i);
                break;
            }
        }
        return funded;
    }
    
    /*
     * This method takes two nodes and a weight as input
     * it then creates a bilateral connection with the two nodes 
     * and sets the edge weight to weight
     */
    public void formConnection(RegisteredNode nodeA, RegisteredNode nodeB, int weight) {
        if(nodeA == null) {
            System.out.println("Error:Node A of the nodes is null!");
            return;
        }
        if ( nodeB == null) {
            System.out.println("Error: Node B of the nodes is null!");
            return;
        }
        if (nodeA.peerNodes.get(nodeB) == null) {
            nodeA.peerNodes.put(nodeB, weight);
        }
        
        if (nodeB.peerNodes.get(nodeA) == null) {
            nodeB.peerNodes.put(nodeA, weight);
        }
    }
    
    private void removePeer(RegisteredNode node1, RegisteredNode node2) {
        node1.peerNodes.remove(node2);
        node2.peerNodes.remove(node1);
    }

    /*
     * This method uses the random int generator to 
     * find a candidat that has a peerNodes.size()
     * that is less than the current max
     * Do not need to account for this not existing because unless
     * the overlay is fully formed, there will always be at least one
     */
    private RegisteredNode findCandidatOne() {
        RegisteredNode candidatOne = null;
        while(true) {
            int potentialNodeIndex = rand.nextInt(masterList.size());
            if(masterList.get(potentialNodeIndex).peerNodes.size() < max) {
                candidatOne = masterList.get(potentialNodeIndex);
                break;
            }
        }
        return candidatOne;
    }

    /*
     * This method finds a random RegisteredNode and returns it on three conditions
     * 1.) that node is not candidat1
     * 2.) that node is not a peer of candidat1
     * 3.) that node has peerNodes.size() < max
     * be aware, there may not be a valid candidat2 so we must find a way to break if we go too long
     * there is a hash set of all previously tried indexes, only run that index through
     * the conditions if its not in there already
     */

     private RegisteredNode findCandidate2(RegisteredNode candidatOne) {
        RegisteredNode candidatTwo = null;
        HashSet<Integer> guessSet = new HashSet<>();
        while(guessSet.size() != masterList.size()) {
            int potentialNodeIndex = rand.nextInt(masterList.size());
            //check if its a valid guess
            if(guessSet.add(potentialNodeIndex) == true) {
                //run the conditions
                if((masterList.get(potentialNodeIndex) != candidatOne) && (candidatOne.peerNodes.containsKey(masterList.get(potentialNodeIndex)) == false) && (masterList.get(potentialNodeIndex).peerNodes.size() < max)) {
                    //we found candidat two
                    candidatTwo = masterList.get(potentialNodeIndex);
                    break;
                }

            }
            
        }
        return candidatTwo;
        
     }

     /*
      * this method simply checks if all nodes have peerNode.size() == to max
      if so, it will increment both min and max by one
      if not, it will return so the next candidates can be found
      */

      private void checkForMax() {
        for(int i = 0; i < masterList.size(); ++i) {
            if(masterList.get(i).peerNodes.size() < max) {
                return;
            }
        }
        ++max;
        ++min;
      }

      private int countUnderFundedNodes() {
        int count = 0;
        for(int i = 0; i < masterList.size(); ++i) {
            if(masterList.get(i).peerNodes.size() < max) {
                ++count;
            }
        }
        return count;
      }

    /*
     * this method will act very similarly for findCandidatTwo
     * except it does not care about if its a neighbor to candidatOne
     * because that is why we are in here!
     */

    private RegisteredNode findUnderFundedNode(RegisteredNode candidatOne) {
        RegisteredNode underFundedNode = null;
        HashSet<Integer> guessSet = new HashSet<>();
        while(guessSet.size() != masterList.size()) {
            int potentialNodeIndex = rand.nextInt(masterList.size());
            //run the conditions
            if((masterList.get(potentialNodeIndex) != candidatOne) && masterList.get(potentialNodeIndex).peerNodes.size() < max) {
                underFundedNode = masterList.get(potentialNodeIndex);
                break;
            }
        }
        return underFundedNode;
    }
    
    /*
     * this method first finds a nodeX that either candidatOne or CandidatTwo will connect too
     * it acheives this by looping the masterList from index 0 and returning the first one that 
     * candidatOne OR candidatTwo is NOT connected to
     * The method then calls findNodeY which is the node the other one will connect to
     * then, nodeX and nodeY are disconnected and then re-connected to their appropriate nodes
     */
    
     private void adjustOverlay(RegisteredNode candidatOne, RegisteredNode candidatTwo) {
        RegisteredNode nodeX = null;
        RegisteredNode nodeY = null;
        while(true) {
            //condition for candidatOne
            int potentialNodeIndex = rand.nextInt(masterList.size());
            if((masterList.get(potentialNodeIndex) != candidatOne) && (candidatOne.peerNodes.containsKey(masterList.get(potentialNodeIndex)) == false) && (masterList.get(potentialNodeIndex).peerNodes.size() == max)) {
                //set node X and find node Y
                nodeX = masterList.get(potentialNodeIndex);
                //System.out.println("candidaz found at index = " + potentialNodeIndex);
                nodeY = findNodeY(nodeX, candidatTwo);
                if(nodeY != null) {
                    //disconnect nodeY and nodeX
                    removePeer(nodeX, nodeY);
                    //connect candidat1 to nodeX
                    formConnection(candidatOne, nodeX, 0);
                    //connect candidat2 to nodeY
                    formConnection(candidatTwo, nodeY, 0);
                    return;
                } 
                //condition for candidat Two
            } if((masterList.get(potentialNodeIndex) != candidatTwo) && (candidatTwo.peerNodes.containsKey(masterList.get(potentialNodeIndex)) == false) && (masterList.get(potentialNodeIndex).peerNodes.size() == max)) {
                //set nodeX and find node Y
                nodeX = masterList.get(potentialNodeIndex);
               // System.out.println("candidaz found at index = " + potentialNodeIndex);
                nodeY = findNodeY(nodeX, candidatOne);
                if(nodeY != null) {
                    removePeer(nodeX, nodeY);
                    formConnection(candidatTwo, nodeX, 0);
                    //connect candidatOne to nodeY
                    formConnection(candidatOne, nodeY, 0);
                    return;
                }    
            }
           //FOR THE BENCH MARK FOUR WE NEED A CASE THAT DEALS WITH NO VALID NODES CAN BE FOUND, ITS VERY RARE

        }
    }

    private RegisteredNode findNodeY(RegisteredNode nodeX, RegisteredNode otherCandidat) {
        //we need to find a nodeY that is a peer of nodeX that otherCandidat is not connected to
        RegisteredNode nodeY = null;
        for(Map.Entry<RegisteredNode, Integer> entry : nodeX.peerNodes.entrySet()) {
            if((entry != otherCandidat) && (otherCandidat.peerNodes.containsKey(entry.getKey()) == false) && entry.getKey().peerNodes.size() == max) {
                nodeY = entry.getKey();
                break;
            }
        }

        return nodeY;
    }

    public boolean hasPartitions() {
        //call DFS
        ArrayList<RegisteredNode> visistedNodes = new ArrayList<>();
        DFS(masterList.get(0), visistedNodes);

        System.out.println();

        return masterList.size() == visistedNodes.size();
    }

    private void DFS(RegisteredNode current, ArrayList<RegisteredNode> visitedNodes) {
        visitedNodes.add(current);

        for(RegisteredNode peerNode : current.peerNodes.keySet()) {
            if(!visitedNodes.contains(peerNode)) {
                DFS(peerNode, visitedNodes);
            }
        }
    }

    public void updateAllLinkWeights() {
        System.out.println("he;;p");
        for(int i = 0; i < masterList.size(); i++) {
            RegisteredNode current = masterList.get(i);
            for(RegisteredNode entry : current.peerNodes.keySet()) {
                if(current.peerNodes.get(entry) == 0 && entry.peerNodes.get(current) == 0) {
                    
                    int weight = rand.nextInt(10) + 1;
                    current.peerNodes.replace(entry, weight);
                    //this would update the weight from A to be only we need to update B to A
                    entry.peerNodes.replace(current, weight);
                    //generate a connection message for both of them
                    String messageOne = current.ip + ":" + current.portNum + " " + entry.ip + ":" + entry.portNum + "-" + weight;
                    
                    linkMessages.add(messageOne);    
                }           
            }
        }
    }
 }
