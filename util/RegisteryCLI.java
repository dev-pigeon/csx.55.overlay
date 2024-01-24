package util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import transport.*;
import wireformats.*;
import node.*;
public class RegisteryCLI implements Runnable {

    //input handler will need a reference to the registries
    //list of registered nodes for responding to commands, input handler will also need a sender object

    int count = 0;
    Scanner scan = new Scanner(System.in);
    
    Registry registry;
    

    public RegisteryCLI(Registry registry) {
        this.registry = registry;
    }

    @Override
    public void run() {
        System.out.println("i am the registry cli and the current size of registered nodes is " + registry.registeredNodes.size());

       while(true) {
        String command = scan.nextLine();
        //call handle command
        handleCommand(command);
       }
    }

    private void handleCommand(String commmandEntered) {
        if(commmandEntered.contains("setup-overlay")) {
            handleOverlaySetup(commmandEntered);
        } else if(commmandEntered.equals("list-messaging-nodes")) {
            registry.listRegisteredNodes();
        } else if(commmandEntered.contains("start")) {
            handleStartProtocol(commmandEntered);
        }
    }


    private void handleOverlaySetup(String overlayCommand) {
        //this will extract the number of connections and then return that to a method in Registry
        int numberOfConnections = Integer.parseInt(overlayCommand.substring(14, overlayCommand.length()));
        registry.setupOverlayProtocol(numberOfConnections);
    }

    private void handleStartProtocol(String startCommand)  {
        int rounds = Integer.parseInt(startCommand.substring(6,startCommand.length()));
        registry.initiateMessagingNodes(rounds);
    }
    
}
