package server;

import command.*;
import constants.Constants;
import utils.Utils;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.concurrent.Executors;

public class RedisSlave extends Redis{
    String masterHost;
    Socket masterSocket;
    int masterPort;
    String role = "slave";
//    int MasterPort;

    public RedisSlave(){
        super();
    }

    public void setMasterHost(String masterHost){
        this.masterHost = masterHost;
    }

    public void setMasterPort(int MasterPort){
        this.masterPort = MasterPort;
    }
    @Override
    public void startServer(){
        executorService = Executors.newCachedThreadPool();
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            serverSocket.setReuseAddress(true);

            while (!serverSocket.isClosed()) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    executorService.execute(() -> handle(clientSocket));
                } catch (Exception e) {
                    System.out.println("IOException: " + e.getMessage());
                }
            }
        } catch (IOException e) {
            System.out.println("IOException: " + e.getMessage());
        } finally {
            executorService.shutdown();
        }
    }
    @Override
    void handle (Socket clientSocket){
        try (InputStream inputStream = clientSocket.getInputStream()){

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = clientSocket.getOutputStream();

            String command;

            while ((command = reader.readLine()) != null) {
                String f = String.format("role is %s, command is %s", this.role, command);
                System.out.println(f);
                if (command.startsWith("*")){
                    int numOfItems = Integer.parseInt(command.substring(1));
                    ArrayList<String> inputs = new ArrayList<>(numOfItems * 2);
                    for (int i =0; i < numOfItems * 2; i++){
                        inputs.add(reader.readLine());
                    }
                    System.out.println("inputs for slave, size of input is: " + inputs.size());
//                    inputs.stream().forEach(System.out::println);
                    String cmd = inputs.get(1);
                    switch (cmd.toLowerCase()) {
                        case Constants.CMD_PING ->
                                outputStream.write( new Ping().print(inputs, cache));
                        case Constants.CMD_ECHO ->
                                outputStream.write( new Echo().print(inputs,cache));
                        case Constants.CMD_SET ->{
                            outputStream.write(new Set().print(this, inputs, cache));
                        }
                        case Constants.CMD_GET ->{
                            System.out.println("does it go into here?");
                            outputStream.write(new Get().print(inputs, cache));
                        }

                        case Constants.CMD_INFO ->{
                            if (inputs.get(3).equalsIgnoreCase("replication")){
                                outputStream.write(new Info(this).print(inputs,cache));
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            System.out.println("Issue occurred in handle " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e){
                System.out.println("could not close socket " + e.getMessage());
            }
        }

    }

    public void connectToMaster(){
//        System.out.println(
//                String.format("connect to Master: value of masterHost: %s, value of masterip: %s", masterHost,masterPort));
        try{
            masterSocket = new Socket(this.masterHost, this.masterPort);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(masterSocket.getOutputStream()));
            String pingCmd = Utils.toRESP(new String[] {"PING"});
            String replCmd1 = Utils.toRESP(new String[] {"REPLCONF","listening-port", Integer.toString(this.port)});
            String replCmd2 = Utils.toRESP(new String[] {"REPLCONF","capa", "psync2"});
            String psyncCmd = Utils.toRESP(new String[] {"PSYNC", "?", "-1"});
            writer.print(pingCmd);
            writer.print(replCmd1);
            writer.print(replCmd2);
            writer.print(psyncCmd);
            writer.flush();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
