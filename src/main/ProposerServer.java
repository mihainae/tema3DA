package main;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.concurrent.locks.ReentrantLock;

public class ProposerServer {
    private ArrayList<Peer> clients;

    private ReentrantLock clientsLock;
    private ReentrantLock sendingLock;
    private ServerSocket ss;

    public ProposerServer(int portNumber) throws IOException {
        clients = new ArrayList<Peer>();

        clientsLock = new ReentrantLock();
        sendingLock = new ReentrantLock();

        ss = new ServerSocket(portNumber);

        while (true) {
            new ServerThread(ss.accept());
        }
    }

    class ServerThread extends Thread {
        private final Socket socket;
        private Peer clientNode;

        public ServerThread(Socket socket) {
            this.socket = socket;

            start();
        }

        public void run() {
            try {
                clientNode = new Peer(new ObjectOutputStream(
                        socket.getOutputStream()));

                clientsLock.lock();
                try {
                    clients.add(clientNode);
                } finally {
                    clientsLock.unlock();
                }

                ObjectInputStream in = new ObjectInputStream(
                        socket.getInputStream());
                while (true) {
                    Message message = (Message) in.readObject();

                    sendingLock.lock();

                    int numberOffElements = 0;

                    for (Message msg : clientNode.outgoing) {
                        if (msg.stateNumber < message.stateNumber) {
                            numberOffElements++;
                        } else {
                            break;
                        }
                    }

                    for (int i = 0; i < numberOffElements; i++) {
                        clientNode.outgoing.remove(0);
                    }

                    for (int i = 0; i < clientNode.outgoing.size(); i++) {
                        /*ArrayList<Message> result = Jupiter.xForm(message,
                                clientNode.outgoing.get(i));
                        message = result.get(0);

                        clientNode.outgoing.remove(i);
                        clientNode.outgoing.add(i, result.get(1));*/
                        ;
                    }

                    for (Peer client : clients) {
                        if (!client.equals(clientNode)) {
                            Message msg;
                            Message msg2;

                            if (message.type == 'i') {
                                msg = new Message(message.position,
                                        message.inserted, client.otherMsg);
                                msg2 = new Message(message.position,
                                        message.inserted, client.myMsg);
                            } else {
                                msg = new Message(message.position,
                                        client.otherMsg);
                                msg2 = new Message(message.position,
                                        client.myMsg);
                            }

                            client.out.writeObject(msg);
                            client.outgoing.add(msg2);
                            client.myMsg++;
                        }
                    }

                    clientNode.otherMsg += 1;

                    sendingLock.unlock();
                }
            } catch (Throwable t) {
                clients.remove(clientNode);
                try {
                    clientNode.out.close();
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if (clients.size() == 0) {
                    System.exit(0);
                }
            }
        }
    }

    public static void main(String[] args) throws NumberFormatException,
            IOException {
        @SuppressWarnings("unused")
        ProposerServer seq = new ProposerServer(Integer.parseInt(args[1]));
    }

    class Peer {
        public ObjectOutputStream out;
        public int myMsg;
        public int otherMsg;
        public ArrayList<Message> outgoing;

        public Peer(ObjectOutputStream out) {
            this.out = out;
            myMsg = 0;
            otherMsg = 0;

            outgoing = new ArrayList<Message>();
        }
    }
}
