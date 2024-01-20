package util;

import java.util.Scanner;

public class InputHandler implements Runnable {

    int count = 0;
    Scanner scan = new Scanner(System.in);
    @Override
    public void run() {
        System.out.println("i am input handler thread and I was started");
       while(true) {
        String command = scan.nextLine();
        System.out.println("You have " + ++count + " commands entered");
       }
    }
    
}
