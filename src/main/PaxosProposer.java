package main;

import java.io.*;
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
    private Hashtable<Integer, Integer> idNumbers;
    private int id;
    private ArrayList<String> commands;

    public PaxosProposer(int serverPort, ArrayList<Integer> peerPorts, ArrayList<Integer> acceptorPorts, int id) throws UnknownHostException,
            IOException {

        this.id = id;

        peersLock = new ReentrantLock();
        new ReentrantLock();
        sendLock = new ReentrantLock();
        peers = new ArrayList<ObjectOutputStream>();
        idNumbers = new Hashtable<Integer, Integer>();
        commands = new ArrayList<String>();

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
            ToProposer toProposer = new ToProposer(this.id, serverPort);
            outToPeer.writeObject(toProposer);
        }

        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        /*
        System.out.println("On proposer:" + serverPort);
        for(Integer peerPort : peerPorts) {
            System.out.println(idNumbers.get(peerPort));
        }
        System.out.println();
        */

        int maxId = 0;
        for(Integer peerPort : peerPorts) {
            if(idNumbers.get(peerPort) > maxId)
                maxId = idNumbers.get(peerPort);
        }
        if(this.id > maxId) {
            maxId = this.id;
        }
        System.out.println("Max ID: " + maxId);
        if(maxId == this.id) {
            System.out.println("On proposer:" + serverPort + " I am the distinguished proposer.");

            readFile();
            for(int i = 0; i < commands.size(); i++) {
                //System.out.println(commands.get(i));
                ToAcceptor toAcceptor = new ToAcceptor("prepare", i);
                int prepareNumber = 0;
                String command = null;
                for(int j = 0; j < acceptorPorts.size(); j++) {
                    //System.out.println(acceptorPorts.get(j));
                    Socket socket = new Socket("localhost", acceptorPorts.get(j));
                    ObjectOutputStream outToAcceptor = new ObjectOutputStream(socket.getOutputStream());
                    ObjectInputStream inFromAcceptor = new ObjectInputStream(socket.getInputStream());
                    outToAcceptor.writeObject(toAcceptor);
                    ToProposer message = null;
                    try {
                        message = (ToProposer) inFromAcceptor.readObject();
                        if(message.getType().equals("promise")) {
                            //System.out.println("Received message: " + message.getMessage() + " " + message.getMaxRound());
                            prepareNumber++;
                            command = commands.get(i);
                        }
                        if(message.getType().equals("replace"))
                            command = message.getAcceptedValue();

                    } catch (ClassNotFoundException e) {
                        e.printStackTrace();
                    }
                }

                if(prepareNumber > acceptorPorts.size()/2+1) {

                    toAcceptor = new ToAcceptor("accept", i, command);
                    int acceptedNumber = 0;
                    for(int j = 0; j < acceptorPorts.size(); j++) {
                        Socket socket = new Socket("localhost", acceptorPorts.get(j));
                        ObjectOutputStream outToAcceptor = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream inFromAcceptor = new ObjectInputStream(socket.getInputStream());
                        outToAcceptor.writeObject(toAcceptor);

                        ToProposer message = null;
                        try {
                            message = (ToProposer) inFromAcceptor.readObject();
                            if(message.getType().equals("accept")) {
                                //System.out.println("Received message: " + message.getMessage() + " " + message.getMaxRound());
                                if(message.getMaxRound() <= i){
                                    acceptedNumber++;
                                    command = commands.get(message.getMaxRound());
                                }
                                else {
                                    i--;
                                }
                            }

                        } catch (ClassNotFoundException e) {
                            e.printStackTrace();
                        }
                    }
                    System.out.println("AcceptedNumber: " + acceptedNumber + " for command " + command);

                    toAcceptor = new ToAcceptor("commit", i, command);
                    for(int j = 0; j < acceptorPorts.size(); j++) {
                        Socket socket = new Socket("localhost", acceptorPorts.get(j));
                        ObjectOutputStream outToAcceptor = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream inFromAcceptor = new ObjectInputStream(socket.getInputStream());
                        outToAcceptor.writeObject(toAcceptor);
                    }

                    toAcceptor = new ToAcceptor("reset", 0);
                    for(int j = 0; j < acceptorPorts.size(); j++) {
                        Socket socket = new Socket("localhost", acceptorPorts.get(j));
                        ObjectOutputStream outToAcceptor = new ObjectOutputStream(socket.getOutputStream());
                        ObjectInputStream inFromAcceptor = new ObjectInputStream(socket.getInputStream());
                        outToAcceptor.writeObject(toAcceptor);
                    }
                }


            }
        }
        else {
            System.out.println("On proposer:" + serverPort + " I am not the distinguished proposer.");
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

                    ToProposer message = (ToProposer) in.readObject();
                    sendLock.lock();
                    //messages.add(message);

                    System.out.println("Received message on " + serverSocket.getLocalPort() + ": " + message.getId());
                    idNumbers.put(message.getPort(), message.getId());

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

    private void readFile() {


        try {
            BufferedReader input = new BufferedReader(
                    new FileReader("commands"));
            try {
                String line = null;

                while ((line = input.readLine()) != null) {

                    String command = "";
                    if (line.substring(0, 3).equals("del")) {
                        command += "del:";
                        command += line.substring(4, 5);
                    }

                    if (line.substring(0, 3).equals("ins")) {
                        command += "ins:";
                        command += line.substring(5, 6)
                                + ":";
                        command += line.substring(8, 9);
                    }
                    System.out.println(line + " -> " + command);
                    commands.add(command);
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }
}
