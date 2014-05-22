package main;

import java.io.IOException;
import java.util.ArrayList;

public class MainProposer {

    public static void main(String args[]) throws IOException {
        int algorithm = Integer.parseInt(args[0]);
        int numberOfProposers = Integer.parseInt(args[1]);
        int serverPort = Integer.parseInt(args[2]);
        ArrayList<Integer> peerPorts;
        ArrayList<Integer> acceptorPorts;
        Algorithm alg = null;

        if(args.length > 0) {
            acceptorPorts= new ArrayList<Integer>();
            peerPorts= new ArrayList<Integer>();
            for(int i = 2; i < args.length; i++) {
                acceptorPorts.add(Integer.parseInt(args[i]));
                System.out.println("MainProposer:" + Integer.parseInt(args[i]));
            }
            //alg = new Dopt(serverPort, ports);
            System.out.println("MainProposer: " + serverPort);

            for(int i = 0; i < numberOfProposers; i++) {
                if(serverPort != 4040 + i)
                    peerPorts.add(4040 + i);
            }

            PaxosProposer paxosProposer = new PaxosProposer(serverPort, peerPorts, acceptorPorts, serverPort);

        }
    }
}
