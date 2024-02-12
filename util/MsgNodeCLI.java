package csx55.overlay.util;

import java.io.IOException;
import java.util.Scanner;
import csx55.overlay.node.MessagingNode;

public class MsgNodeCLI implements Runnable {
    Scanner scan = new Scanner(System.in);

    MessagingNode msgNode;

    volatile boolean done = false;

    public MsgNodeCLI(MessagingNode node) {
        this.msgNode = node;
    }

    @Override
    public void run() {
       while(!done) {
        String command = scan.nextLine();
        try {
            handleCommand(command);
        } catch(IOException ioe) {
            System.out.println(ioe.getMessage());
        }
       }

       System.out.println("DEBUG: client cli shut down");

    }

    private void handleCommand(String message) throws IOException {
        if(message.equals("print-shortest-path")) {
            //call some stuff in MessagingNode
        } else if(message.equals("exit-overlay")) {
            msgNode.sendDeregisterRequest();
        } else if(message.equals("list-weights")) {
            msgNode.listWeights();
        } else if(message.equals("test-djikstra")) {
            msgNode.testDjikstra();
        }
    }

    public void toggleDone() {
        this.done = true;
    }
}
