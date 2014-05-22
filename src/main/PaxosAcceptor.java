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

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private int myMsg;
    private int otherMsg;
    private ArrayList<Message> outgoing;

    public PaxosAcceptor (int portNumber) throws IOException {

        super();

        socket = new Socket("localhost", portNumber);

        out = new ObjectOutputStream(socket.getOutputStream());
        in = new ObjectInputStream(socket.getInputStream());

        outgoing = new ArrayList<Message>();

        myMsg = 0;
        otherMsg = 0;

        /*new ReceiverThread();*/
    }

    @Override
    public void addChar(String message) throws IOException {

    }

    @Override
    public void removeChar(String message) throws IOException {

    }
}
