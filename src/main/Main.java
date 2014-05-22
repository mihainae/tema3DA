package main;

import java.io.IOException;
import java.util.ArrayList;

public class Main {

    public static void main(String args[]) throws IOException {
        int algorithm = Integer.parseInt(args[0]);
        int generatorPort = Integer.parseInt(args[1]);
        int serverPort = Integer.parseInt(args[2]);
        ArrayList<Integer> ports;
        Algorithm alg = null;

        if(args.length > 0) {

            System.out.println("Main: " + serverPort);
            alg = new PaxosAcceptor(serverPort);


            @SuppressWarnings("unused")
            UI ui = new UI(alg, generatorPort);
        }
    }
}
