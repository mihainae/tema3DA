package main;

import java.io.Serializable;

public class Message implements Serializable {

	private static final long serialVersionUID = 23L;
	public char type;
	public int stateNumber;
	public int position;
	public char inserted;

	public Message(int position, int stateNumber) {
		this.type = 'r';
		this.stateNumber = stateNumber;
		this.position = position;
		this.inserted = ' ';
	}

	public Message(int position, char inserted, int stateNumber) {
		this.type = 'i';
		this.stateNumber = stateNumber;
		this.position = position;
		this.inserted = inserted;
	}

	public String getMessage() {
		String result;
		if (type == 'i') {
			result = "ins:" + inserted + ":" + position;
		} else {
			result = "rem:" + position;
		}
		
		return result;
	}

	@Override
	public String toString() {
		return "message: " + getMessage() + ", type: " + type
				+ ", stateNumber: " + stateNumber;
	}

	public int size() {
		int size = 0;
		if (type == 'i') {
			size = 2 * Integer.SIZE + 2;
		} else {
			size = 2 * Integer.SIZE + 1;
		}
		return size;
	}

}