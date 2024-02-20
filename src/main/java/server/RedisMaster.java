package server;

import command.*;
import constants.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.Executors;

public class RedisMaster extends Redis{

    HashSet<Socket> replicaSockets;

    public RedisMaster(){
        this.masterReplid = randomId();
        this.masterReplOffset = 0;
        replicaSockets = new HashSet<>();
    }
    public String getMasterReplid() {
        return this.masterReplid;
    }

    public int getMasterReplOffset(){
        return this.masterReplOffset;
    }

    @Override
    public void startServer(){
        executorService = Executors.newCachedThreadPool();
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            serverSocket.setReuseAddress(true);

            while (!serverSocket.isClosed()) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    System.out.println("clientSocket in redis master startServer: " + clientSocket.toString());
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
            System.out.println("clientSocket info in redis master startServer: " + clientSocket);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = clientSocket.getOutputStream();

            String command;
            while ((command = reader.readLine()) != null) {

                if (command.startsWith("*")){
                    int numOfItems = Integer.parseInt(command.substring(1));
                    //0 = size of 1, 1 = cmd, 2 = size of 3, 3 = key/echo val, 4 = size of set, 5 = setVal
                    ArrayList<String> inputs = new ArrayList<>(numOfItems * 2);
                    for (int i =0; i < numOfItems * 2; i++){
                        inputs.add(reader.readLine());
//                        System.out.println("element " + i + " is: " + inputs.get(i));
                    }
                    String cmd = inputs.get(1);
                    switch (cmd.toLowerCase()) {
                        case Constants.CMD_PING ->{
                                outputStream.write( new Ping().print(inputs, cache));
                            System.out.println("Client socket in ping: " + clientSocket);
                        }
                        case Constants.CMD_ECHO ->
                                outputStream.write( new Echo().print(inputs,cache));
                        case Constants.CMD_SET ->{
//                            System.out.println("entering in replica SET in REDIS MASTER master class");
                            outputStream.write(new Set().print(this, inputs, cache));
                            sendToReplicas(inputs);    
                        }
                        case Constants.CMD_GET ->
                                outputStream.write(new Get().print(inputs, cache));
                        case Constants.CMD_INFO ->{
                            if (inputs.get(3).equalsIgnoreCase("replication")){
                                outputStream.write(new Info(this).print(inputs,cache));
                            }
                        }
                        case Constants.CMD_REPLCONF ->
                            outputStream.write(new ReplConf().print(inputs,cache));
                        case Constants.CMD_PSYNC -> {
                            System.out.println("Client socket in psync: " + clientSocket.toString());
                            outputStream.write(new Psync().print(masterReplid, String.valueOf(masterReplOffset)));
                            sendRDBFile(outputStream);
                            replicaSockets.add(clientSocket);
                        }
                    }
                }
                outputStream.flush();
            }
        } catch (Exception e) {
            System.out.println("Issue occurred in handle " + e.getMessage());
        } finally {
            try {
                clientSocket.close();
            } catch (IOException e){
                System.out.println("could not close socket " + e.getMessage());
            }
            System.out.println("DONE in handle");
        }

    }

    private void sendToReplicas(ArrayList<String> inputs) {

        for (Socket socket: replicaSockets){
//            System.out.println(socket.toString());
            System.out.println("clientSocket in sendToReplica: " + socket.toString());

            try{
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(outputStream, true);
                for (String s: inputs){
                    if (s.startsWith("$")){
                        continue;
                    }
                    pw.println("SET\r\n");
                }

            } catch (IOException e) {
                throw new RuntimeException("Issue in send replicas: " + e);
            }
        }
    }

    private void sendRDBFile(OutputStream outputStream){

        byte[] decoded_db = Base64.getDecoder().decode(Constants.EMPTY_DB);
        String prefix = "$" + decoded_db.length + Constants.R_N;
        byte[] prefixBytes = prefix.getBytes();
        try {
            outputStream.write(prefixBytes);
            outputStream.write(decoded_db);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
