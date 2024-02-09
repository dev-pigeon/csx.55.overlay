package csx55.overlay.util;

import csx55.overlay.node.*;

import java.util.ArrayList;

public class ConnectionMessageGenerator {
    
    ArrayList<RegisteredNode> masterList;

    public ConnectionMessageGenerator(ArrayList<RegisteredNode> masterList) {
        this.masterList = masterList;
    }

    public void generateConnectionMessages() {
        formExclusiveLists();
        composeConnectionMessageLists();
    }

    /*
     * this method will loop each node in master list
     * then it will loop their peer nodes
     * at each index of keyset in their peer node list it will check
     * if A is in B's connection list,
     * and since A is being examined, if B is in their list, 
     * B will remove A from their list to ensure no duplicates
     */
    private void formExclusiveLists() {
            for(int i = 0; i < masterList.size(); ++i) {
                RegisteredNode current = masterList.get(i);
                for(RegisteredNode node : current.peerNodes.keySet()) {
                    //check if NODE is contained in CURRENTS conlist
                    if((!current.conectionList.contains(node)) && (!node.conectionList.contains(current))) {
                        current.addMemberToConnectionList(node);
                    }
                }
            }
        }

    private void composeConnectionMessageLists() {
        for(int i = 0; i < masterList.size(); ++i) {
            RegisteredNode current = masterList.get(i);
            for(RegisteredNode node : current.conectionList) {
                String connectionMessage = node.ip + ":" + node.portNum;
                //System.out.println(connectionMessage);
                current.connectionMessageList.add(connectionMessage);
            }
        }
    }

     void printAllSizesofConnectionList() {
        for(int i = 0; i < masterList.size(); ++i)  {
            System.out.println(masterList.get(i).conectionList.size());
        }
        System.out.println();
    }

    void printAllSizesofMsgList() {
        for(int i = 0; i < masterList.size(); ++i) {
            System.out.println("printing for node " + i);
            RegisteredNode current = masterList.get(i);
            for(String msg : current.connectionMessageList) {
                System.out.println(msg);
            }
            System.out.println();
        }
    }

    
}
