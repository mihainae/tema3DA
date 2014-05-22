package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

public class PaxosProposer {
    public ServerSocket serverSocket;
    private ArrayList<ObjectOutputStream> peers;
    private ReentrantLock peersLock;
    private ReentrantLock sendLock;
    private Hashtable<Integer, Integer> status;

    public PaxosProposer(int serverPort, ArrayList<Integer> peerPorts, ArrayList<Integer> acceptorPorts) throws UnknownHostException,
            IOException {


        peersLock = new ReentrantLock();
        new ReentrantLock();
        sendLock = new ReentrantLock();
        peers = new ArrayList<ObjectOutputStream>();

        serverSocket = new ServerSocket(serverPort);

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

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < peerPorts.size(); i++) {
            Socket socket = new Socket("localhost", peerPorts.get(i));
            //new ServerThread(socket);
            ObjectOutputStream outToPeer = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream inFromPeer = new ObjectInputStream(socket.getInputStream());
            outToPeer.writeObject("from " + serverPort);
        }



    }

    class ServerThread extends Thread {

        private final Socket socket;
        private ObjectOutputStream out;

        public ServerThread(Socket socket) {
            this.socket = socket;
            start();
        }

        public void run() {
            try {
                out = new ObjectOutputStream(socket.getOutputStream());

                //out.writeObject(String.valueOf(serverSocket.getLocalPort()));

                peersLock.lock();
                try {
                    peers.add(out);
                } finally {
                    peersLock.unlock();
                }

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                //int port = Integer.parseInt((String) in.readObject());

                while (true) {

                    String message = (String) in.readObject();
                    sendLock.lock();
                    //messages.add(message);
                    System.out.println("Received message on " + serverSocket.getLocalPort() + ": " + message);

                    sendLock.unlock();
                }
            } catch (java.net.SocketException t) {
                peersLock.lock();
                try {
                    peers.remove(out);
                } finally {
                    peersLock.unlock();
                }
                try {
                    out.close();
                    socket.close();
                    System.exit(0);
                } catch (IOException e) {
                    e.printStackTrace();
                }

            } catch (IOException e) {
                e.printStackTrace();
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}
