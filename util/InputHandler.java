package util;

import java.util.ArrayList;
import java.util.Scanner;

import transport.*;
import wireformats.*;
import node.*;
public class InputHandler implements Runnable {

    //input handler will need a reference to the registries
    //list of registered nodes for responding to commands, input handler will also need a sender object

    int count = 0;
    Scanner scan = new Scanner(System.in);
    
    ArrayList<RegisteredNode> registeredNodes;

    public InputHandler() {
        this.registeredNodes = registeredNodes;
    }

    @Override
    public void run() {
        System.out.println("i am input handler thread and I was started");

       while(true) {
        String command = scan.nextLine();
        System.out.println("You have " + ++count + " commands entered");
       }
    }
    
}
