package main;

import java.io.Serializable;

public class ToProposer implements Serializable{

    public int id;
    public int port;

    public ToProposer(int id, int port) {
        this.id = id;
        this.port = port;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    public void setPort(int port) {
        this.port = port;
    }

    public int getPort() {
        return port;
    }
}
