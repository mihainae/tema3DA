package main;

import java.io.Serializable;

public class ToProposer implements Serializable{

    public int id;
    public int port;
    public String message;
    public String type;
    public int maxRound;
    public Integer acceptedProposal;
    public String acceptedValue;

    public ToProposer() {
        ;
    }

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

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getType() {
        return type;
    }

    public void setMaxRound(int maxRound) {
        this.maxRound = maxRound;
    }

    public int getMaxRound() {
        return maxRound;
    }

    public void setAcceptedProposal(Integer acceptedProposal) {
        this.acceptedProposal = acceptedProposal;
    }

    public Integer getAcceptedProposal() {
        return acceptedProposal;
    }

    public void setAcceptedValue(String acceptedValue) {
        this.acceptedValue = acceptedValue;
    }

    public String getAcceptedValue() {
        return acceptedValue;
    }
}
