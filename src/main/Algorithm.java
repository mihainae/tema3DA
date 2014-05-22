package main;
import java.io.IOException;
import java.util.Observable;
import java.util.concurrent.locks.ReentrantLock;

public abstract class Algorithm extends Observable {

    private int totalTraffic;
    private ReentrantLock trafficLock;

    public Algorithm() {
        totalTraffic = 0;
        trafficLock = new ReentrantLock();
    }

    public void processMessage(String message) throws IOException {
        String[] elements = message.split(":");
        if (elements[0].equals("ins")) {
            addChar(message);
        }

        if (elements[0].equals("del")) {
            removeChar(message);
        }
    }

    public void addChar(String s, int position) throws IOException {
        String message = "ins:" + s + ":" + position;
        this.addChar(message);
    }

    public void removeChar(int position) throws IOException {
        String message = "del:" + position;
        this.addChar(message);
    }

    public int getTotalTraffic() {
        return totalTraffic;
    }

    protected void incrementTraffic(int ammount) {
        trafficLock.lock();
        try {
            totalTraffic += ammount;
        } finally {
            trafficLock.unlock();
        }
    }

    public abstract void addChar(String message) throws IOException;

    public abstract void removeChar(String message) throws IOException;

}
