package server;

import command.*;
import constants.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashSet;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RedisMaster extends Redis{

    HashSet<Socket> replicaSockets;

    String dir;
    String dbname;

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

    public void setDir(String dir) {
        this.dir = dir;
    }
    public void setDbname(String dbname){
        this.dbname =dbname;
    }

    @Override
    public void startServer(){
        executorService = Executors.newCachedThreadPool();
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            serverSocket.setReuseAddress(true);

            while (!serverSocket.isClosed()) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    executorService.execute(() -> {
                        handle(clientSocket);
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

    @Override
    void handle (Socket clientSocket){
        try (InputStream inputStream = clientSocket.getInputStream()){
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = clientSocket.getOutputStream();

            String command;
            while ((command = reader.readLine()) != null) {

                if (command.startsWith("*")){
                    int numOfItems = Integer.parseInt(command.substring(1));
                    ArrayList<String> inputs = new ArrayList<>(numOfItems * 2);
                    for (int i =0; i < numOfItems * 2; i++){
                        inputs.add(reader.readLine());
                    }
                    String cmd = inputs.get(1);
                    switch (cmd.toLowerCase()) {
                        case Constants.CMD_PING ->{
                            outputStream.write( new Ping().print(inputs, cache));
                        }
                        case Constants.CMD_ECHO ->
                                outputStream.write( new Echo().print(inputs,cache));
                        case Constants.CMD_SET ->{
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
                            outputStream.write(new Psync().print(masterReplid, String.valueOf(masterReplOffset)));
                            sendRDBFile(outputStream);
                            replicaSockets.add(clientSocket);
                        }
                        case "config" -> {
                            if (inputs.get(3).equalsIgnoreCase("get")){
                                String print = "*2\r\n";
                                if(inputs.get(5).toLowerCase().equals("dir")){
                                    print += String.format("$3\r\ndir\r\n$%s\r\n%s\r\n",dir.length(), dir );
                                    outputStream.write(print.getBytes());
                                } else if (inputs.get(5).toLowerCase().equals("dbfilename")) {
                                    print += String.format("$10\r\ndbfilename\r\n$%s\r\n%s\r\n",dbname.length(), dbname);
                                    outputStream.write(print.getBytes());
                                }
                            }
                        }
                        case "keys" -> {
                            if (inputs.get(3).equals("*")){
                                byte[] bytes = Files.readAllBytes(Path.of(dir, dbname));
                                String str = new String(bytes);
                                ByteBuffer buffer = ByteBuffer.wrap(bytes);

                            }
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
        }

    }

    private void sendToReplicas(ArrayList<String> inputs) {

        for (Socket socket: replicaSockets){
            try{
                OutputStream outputStream = socket.getOutputStream();
                PrintWriter pw = new PrintWriter(outputStream, true);
                int arraySize=inputs.size() / 2;
                String first = "*"+Integer.toString(arraySize) + "\r\n";
                pw.print(first);
                for (String s: inputs){
//                  pw.println("*3\r\n$3\r\nset\r\n$3\r\nfoo\r\n$3\r\n123\r\n");
                    pw.print(s+"\r\n");
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
