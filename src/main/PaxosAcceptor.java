package main;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

public class PaxosAcceptor extends Algorithm{

    public ServerSocket serverSocket;
    private ArrayList<ObjectOutputStream> peers;
    private ReentrantLock peersLock;
    private ReentrantLock sendLock;

    public PaxosAcceptor (int serverPort, ArrayList<Integer> peerPorts) throws IOException {

        super();
        serverSocket = new ServerSocket(serverPort);

        peers = new ArrayList<ObjectOutputStream>();
        peersLock = new ReentrantLock();
        new ReentrantLock();
        sendLock = new ReentrantLock();

        /*
        new Thread(new Runnable() {
            public void run() {
                try {
                    while(true) {
                        new ServerThread(serverSocket.accept());
                    }
                } catch (IOException e) {
                }
            }
        }).start();

        new SenderThread().start();
        */
    }

    @Override
    public void addChar(String message) throws IOException {

    }

    @Override
    public void removeChar(String message) throws IOException {

    }
}
