package main;

import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;

public class Starter {
    public static int numberOfEditors = 3;
    public static int numberOfProposers = 3;
    public static ArrayList<String> parameters;
    private static ArrayList<Integer> generatorPorts;
    private static ArrayList<String> acceptorsPorts;

    public static void main(String args[])  {

        parameters = new ArrayList<String>();
        generatorPorts = new ArrayList<Integer>();
        acceptorsPorts = new ArrayList<String>();

        startPaxos();

        try {
            Thread.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        ArrayList<ObjectOutputStream> editorsOut = new ArrayList<ObjectOutputStream>();
        ArrayList<ObjectInputStream> editorsIn = new ArrayList<ObjectInputStream>();
        for (Integer port : generatorPorts) {
            try {
                System.out.println("Starter: " +port);
                Socket socket = new Socket("localhost", port);
                editorsOut.add(new ObjectOutputStream(socket
                        .getOutputStream()));
                editorsIn.add(new ObjectInputStream(socket
                        .getInputStream()));

            } catch (UnknownHostException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

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
                    int clientNumber = (int) ((Math.random() * 10) % editorsOut.size());
                    editorsOut.get(clientNumber).writeObject(command);

                }

                for (ObjectOutputStream editor : editorsOut) {
                    try {
                        editor.writeObject("execute");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            } finally {
                input.close();
            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }

    }

    private static void startPaxos(){

        int currentPort = 2020;
        int generatorPort = 3000;
        ArrayList<String> ports = new ArrayList<String>();
        for(int i = 0; i < numberOfEditors; i++) {

            parameters.clear();
            parameters.add(String.valueOf(generatorPort));
            parameters.add(String.valueOf(currentPort));
            parameters.addAll(ports);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        Starter.exec(Main.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            ports.add(String.valueOf(currentPort));
            generatorPorts.add(generatorPort);
            acceptorsPorts.add(String.valueOf(currentPort));


            currentPort++;
            generatorPort++;
        }

        currentPort = 4040;
        for(int i = 0; i < numberOfProposers ; i++) {
            parameters.clear();
            parameters.add(String.valueOf(numberOfProposers));
            parameters.add(String.valueOf(currentPort));
            parameters.addAll(acceptorsPorts);

            new Thread(new Runnable() {
                public void run() {
                    try {
                        Starter.exec(MainProposer.class);
                    } catch (IOException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            currentPort++;

        }
    }

    private static int exec(@SuppressWarnings("rawtypes") Class klass) throws IOException, InterruptedException {
        String javaHome = System.getProperty("java.home");
        String javaBin = javaHome + File.separator + "bin" + File.separator + "java";
        String classpath = System.getProperty("java.class.path");
        String className = klass.getCanonicalName();

        ProcessBuilder builder;

        ArrayList<String> params = new ArrayList<String>();
        params.add(javaBin);
        params.add("-cp");
        params.add(classpath);
        params.add(className);
        params.add("1");
        params.addAll(parameters);

        builder = new ProcessBuilder(params);

        Process process = builder.start();
        pipeOutput(process);
        process.waitFor();

        return process.exitValue();
    }

    private static void pipeOutput(Process process) {
        pipe(process.getErrorStream(), System.err);
        pipe(process.getInputStream(), System.out);
    }

    private static void pipe(final InputStream src, final PrintStream dest) {
        new Thread(new Runnable() {
            public void run() {
                try {
                    byte[] buffer = new byte[1024];

                    for (int n = 0; n != -1; n = src.read(buffer)) {
                        dest.write(buffer, 0, n);
                    }
                } catch (IOException e) { // just exit
                }
            }
        }).start();
    }
}
