package main;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Observable;
import java.util.Observer;

public class UI implements Observer {
	private JFrame jf;

	private JTextArea textArea;
	private JScrollPane scrollPane;
	private Algorithm algorithm;
	private String text;
	private Runnable updateText;
	private DocumentListener listener;
	private int caretPosition;
	private int generatorPort;
	private ServerSocket generatorSocket;

	public UI(Algorithm algorithm, int generatorPort) {
		this.algorithm = algorithm;
		this.algorithm.addObserver(this);
		this.generatorPort = generatorPort;

		try {
			generatorSocket = new ServerSocket(this.generatorPort);
		} catch (IOException e) {
			e.printStackTrace();
		}

		jf = new JFrame();

		jf.setSize(600, 600);
		jf.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		jf.setVisible(true);

		textArea = new JTextArea();
		textArea.setColumns(20);
		textArea.setLineWrap(true);
		textArea.setRows(5);
		textArea.setWrapStyleWord(true);
		listener = new EventListener(textArea, algorithm);
		textArea.getDocument().addDocumentListener(listener);
		scrollPane = new JScrollPane(textArea);

		text = textArea.getText();
		caretPosition = 0;

		updateText = new Runnable() {
			public void run() {
				textArea.getDocument().removeDocumentListener(listener);
				textArea.setText(text);
				textArea.getDocument().addDocumentListener(listener);
				try {
					textArea.setCaretPosition(caretPosition);
				} catch (IllegalArgumentException e) {
					// do nothing
				}
			}
		};

		jf.setLayout(new BoxLayout(jf.getContentPane(), BoxLayout.Y_AXIS));
		jf.add(scrollPane);
		jf.setVisible(true);

		new Thread(new Runnable() {
			public void run() {
				try {
					while (true) {
						new GeneratorThread(generatorSocket.accept());
					}
				} catch (IOException e) {
				}
			}
		}).start();
	}

	@Override
	public void update(Observable o, Object arg) {
		String message = (String) arg;

		String[] elements = message.split(":");

		try {
			if (elements[0].equals("ins")) {
				int middle = Integer.parseInt(elements[2]);
				if (middle > text.length()) {
					middle = text.length();
				}
				text = text.substring(0, middle) + elements[1]
						+ text.substring(middle);
				caretPosition = middle + 1;
			} else {

				int middle = Integer.parseInt(elements[1]);
				if (middle > text.length()) {
					middle = text.length();
				}
				text = text.substring(0, middle) + text.substring(middle + 1);
				caretPosition = middle;
			}
		} catch (StringIndexOutOfBoundsException e) {

		}

		SwingUtilities.invokeLater(updateText);
	}

	class GeneratorThread extends Thread {
		private final Socket socket;

		public GeneratorThread(Socket socket) {
			this.socket = socket;
			;
			start();
		}

		public void run() {
			try {
				ObjectInputStream in = new ObjectInputStream(
						socket.getInputStream());
				ObjectOutputStream out = new ObjectOutputStream(
						socket.getOutputStream());

				ArrayList<String> messages = new ArrayList<String>();
				while (true) {
					String message = (String) in.readObject();
					if (message.equals("traffic")) {
						out.writeObject(algorithm.getTotalTraffic());
					} else if (message.equals("kill")) {
						System.exit(0);
					} else if (message.equals("execute")) {
						for (String msg : messages) {
							algorithm.processMessage(msg);

							int time = (int) ((Math.random() * 10) % 5 + 1) * 100;
							try {
								Thread.sleep(time);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					} else if (message.equals("executeNoDelay")) {
						for (String msg : messages) {
							algorithm.processMessage(msg);
						}
						messages.clear();
					} else if (message.equals("execute1stDelay")) {
						int i = 0;
						for (String msg : messages) {

							/*if (((Dopt) algorithm).serverSocket.getLocalPort() == 2020) {
								try {
									Thread.sleep(10);
								} catch (InterruptedException e) {
									e.printStackTrace();
								}
							}*/
							algorithm.processMessage(msg);
							i++;
						}
						messages.clear();
					} else {
						messages.add(message);
					}
				}
			} catch (java.net.SocketException t) {
				try {
					socket.close();
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

class EventListener implements DocumentListener {

	private JTextArea textArea;
	private Algorithm algorithm;

	public EventListener(JTextArea textArea, Algorithm algorithm) {
		this.textArea = textArea;
		this.algorithm = algorithm;
	}

	@Override
	public void changedUpdate(DocumentEvent arg0) {
	}

	@Override
	public void insertUpdate(DocumentEvent arg0) {
		int offset = arg0.getOffset();
		try {
			String chr = textArea.getText(offset, 1);
            try {
                algorithm.addChar(chr, offset);
            } catch (IOException e) {
                e.printStackTrace();
            }
        } catch (BadLocationException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void removeUpdate(DocumentEvent arg0) {
		int offset = arg0.getOffset();
		for (int i = 0; i < arg0.getLength(); i++) {
			try {
				algorithm.removeChar(offset);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
