package main;

import java.io.Serializable;

public class ToAcceptor implements Serializable{
    public String type;
    public int round;
    public String value;

    public ToAcceptor(String type, int round) {
        this.type = type;
        this.round = round;
    }

    public ToAcceptor(String type, int round, String value) {
        this.type = type;
        this.round = round;
        this.value = value;
    }
}
