package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.concurrent.locks.ReentrantLock;

public class PaxosAcceptor extends Algorithm{

    public ServerSocket serverSocket;
    private ReentrantLock peersLock;
    private ReentrantLock sendLock;

    private Integer minProposal; //the number of the smallest proposal this server will accept, or 0 if it has never received
    //a Prepare request

    private Integer acceptedProposal; //the number of the last proposal the server has accepted, or 0 if it never accepted
    //any

    private String acceptedValue; //the value from the most recent proposal the server has accepted, or null if it has never
    //accepted a proposal

    private Integer maxRound; //the largest round number the server has seen

    public PaxosAcceptor (int serverPort) throws IOException {

        super();
        serverSocket = new ServerSocket(serverPort);

        peersLock = new ReentrantLock();
        new ReentrantLock();
        sendLock = new ReentrantLock();
        minProposal = -1;


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

    }

    @Override
    public void addChar(String message) throws IOException {

    }

    @Override
    public void removeChar(String message) throws IOException {

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

                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());

                //int port = Integer.parseInt((String) in.readObject());

                while (true) {

                    ToAcceptor message = (ToAcceptor) in.readObject();
                    sendLock.lock();
                    //messages.add(message);
                    System.out.println("Received message on ACCEPTOR " + serverSocket.getLocalPort() + ": " + message.type + " round " + message.round);
                    if(message.type.equals("prepare")) {

                        if(acceptedProposal == null) {

                            if(message.round > minProposal) {
                                minProposal = message.round;
                                maxRound = message.round;
                                ToProposer toProposer = new ToProposer();
                                toProposer.setMessage("ACK");
                                toProposer.setMaxRound(maxRound);
                                toProposer.setType("promise");

                                out.writeObject(toProposer);
                            }
                        }
                        else {
                            ToProposer toProposer = new ToProposer();
                            toProposer.setMessage("NACK");
                            toProposer.setAcceptedProposal(acceptedProposal);
                            toProposer.setAcceptedValue(acceptedValue);
                            toProposer.setType("replace");

                            out.writeObject(toProposer);
                        }
                    }
                    if(message.type.equals("accept")) {
                        if(message.round >= minProposal) {
                            acceptedProposal = message.round;
                            acceptedValue = message.value;
                            minProposal = message.round;
                            maxRound = message.round;
                        }
                        ToProposer toProposer = new ToProposer();
                        toProposer.setMaxRound(maxRound);
                        toProposer.setType("accept");
                        out.writeObject(toProposer);
                    }

                    if(message.type.equals("commit")) {
                        setChanged();
                        notifyObservers(message.value);
                        clearChanged();
                    }

                    if(message.type.equals("reset")) {
                        acceptedProposal = null;
                        acceptedValue = null;
                        maxRound = null;
                        minProposal = -1;
                    }
                    sendLock.unlock();
                }
            } catch (java.net.SocketException t) {
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
