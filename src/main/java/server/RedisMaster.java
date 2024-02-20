package server;

import command.*;
import constants.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
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
//                    System.out.println("clientSocket in redis master startServer: " + clientSocket.toString());
                    executorService.execute(() -> {
                        handleClient(clientSocket);
//                        handle(clientSocket);
                    });
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

    public void handleClient(Socket clientSocket){
        try{
            BufferedReader reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            String line;
            ArrayList<String> commandArray = new ArrayList<>();
            int commandLength = 0;
            while ((line = reader.readLine()) != null){
                commandArray.add(line);
                if(commandArray.size() == 1){
                    commandLength = Integer.parseInt(commandArray.getFirst().substring(1))*2+1;
                } else if(commandArray.size() == commandLength){
                    handleCommands(commandArray, clientSocket);
                    commandArray.clear();
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private void handleCommands(ArrayList<String> commandArray, Socket clientSocket) throws IOException {
        // 0=*N, 1=$S, 2=CMD/SET/GET, 3="$S, 4=KEY 5=$S 6=VAL 7=$S 8=px 9=$S 10=$milli
        int commandLength = Integer.parseInt(commandArray.getFirst().substring(1));

        OutputStream os = clientSocket.getOutputStream();
        PrintWriter writer = new PrintWriter(os, true);
        String command = commandArray.get(2);
        switch (command.toLowerCase()){
            case "ping":
                writer.print("+PONG\r\n");
                writer.flush();
                break;
            case "echo":
                System.out.println("ECHO");
                String word = commandArray.get(4);
                writer.print("$"+word.length()+"\r\n"+word+"\r\n");
                writer.flush();
                break;
            case "set":
                if(commandLength==3) {
//                    handleSet(commandArray.get(4), commandArray.get(6), writer);
                    cache.put(commandArray.get(4), commandArray.get(6));
                    writer.print("+OK\r\n");
                    writer.flush();
                }else if(commandLength==5){
                    if(commandArray.get(8).equalsIgnoreCase("px")){
//                        handleSetWithExpiry(commandArray.get(4), commandArray.get(6), commandArray.get(10), writer);
                        Duration expiry = Duration.ofMillis(Long.parseLong(commandArray.get(10)));
                        ExecutorService thread = Executors.newSingleThreadExecutor();
                        thread.execute(() -> {
                            try{
                                Thread.sleep(expiry);
                                cache.remove(commandArray.get(4));
                            } catch (InterruptedException e) {
                                throw new RuntimeException(e);
                            }
                        });
                        writer.print("+OK\r\n");
                        writer.flush();
                    }
                }else{
                    writer.print("-ERR syntax error\r\n");
                    writer.flush();
                }
                sendToReplicas(commandArray);
                break;
            case "get":
//                handleGet(commandArray.get(4), writer);
                String key = commandArray.get(4);
                String value = cache.get(key);
                String result = value == null ? "$-1\r\n" : "+" + value + "\r\n";
                writer.print(result);
                writer.flush();
                break;
            case "info":
                if(commandArray.get(4).equalsIgnoreCase("replication")){
//                    handleInfo(writer);
                    StringBuilder response = new StringBuilder();
                    response.append(String.format("role:%s\r\n", role));
                    response.append(String.format("master_replid:%s\r\n", masterReplid));
                    response.append(String.format("master_repl_offset:%s\r\n", masterReplOffset));
                    String info = response.toString();
                    writer.print("$"+info.length()+"\r\n" + info + "\r\n");
                    writer.flush();
                }
                else{
                    writer.print("-ERR unknown subcommand or wrong number of arguments for 'info'\r\n");
                    writer.flush();
                }
                break;
            case "replconf":
                writer.print("+OK\r\n");
                writer.flush();
                break;
            case "psync":{
                writer.print(String.format("+FULLRESYNC %s %s\r\n", masterReplid, masterReplOffset));
                writer.flush();
            }
            default:
                writer.print("-ERR unknown command '"+command+"'\r\n");
                writer.flush();
                break;
        }
    }

    @Override
    void handle (Socket clientSocket){
        try (InputStream inputStream = clientSocket.getInputStream()){
//            System.out.println("clientSocket info in redis master startServer: " + clientSocket);
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
//                            System.out.println("Client socket in ping: " + clientSocket);
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
//                            System.out.println("Client socket in psync: " + clientSocket.toString());
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
//            System.out.println("DONE in handle");
        }

    }

    private void sendToReplicas(ArrayList<String> inputs) {

        for (Socket socket: replicaSockets){
//            System.out.println(socket.toString());
//            System.out.println("clientSocket in sendToReplica: " + socket.toString());
            try{
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(outputStream, true);
                for (String s: inputs){
//                    if (s.startsWith("$")){
//                        continue;
//                    }
//                    pw.println("*3\r\n$3\r\nset\r\n$3\r\nfoo\r\n$3\r\n123\r\n");
                    pw.print("+OK\r\n");
                }
                pw.flush();
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
