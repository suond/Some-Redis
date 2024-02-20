package server;

import command.*;
import constants.Constants;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Redis {

    int port = 6379;
    ExecutorService executorService;
    Map<String, String> cache = new HashMap<>();
    String role = "master";
    String masterReplid;
    int masterReplOffset = 0;
    String dir;
    String dbname;

    public int getPort(){
        return this.port;
    }
    public void setPort(int port){this.port = port;}

    public String getRole(){
        return this.role;
    }

    public String getMasterReplid() {
        return this.masterReplid;
    }

    public int getMasterReplOffset(){
        return this.masterReplOffset;
    }

    public void setRole(String role){
        this.role = role;
    }
    public void setDir(String dir) {
        this.dir = dir;
    }
    public void setDbname(String dbname){
        this.dbname =dbname;
    }

     public void startServer(){
        executorService = Executors.newCachedThreadPool();
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            serverSocket.setReuseAddress(true);

            while (!serverSocket.isClosed()) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    System.out.println("client info in startServer: " + clientSocket.toString());
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

      void handle (Socket clientSocket){
        try (InputStream inputStream = clientSocket.getInputStream()){

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = clientSocket.getOutputStream();

            String command;
            while ((command = reader.readLine()) != null) {
//                System.out.println("command: " + command);
                String f = String.format("role is %s, command is %s", this.role, command);
                System.out.println(f);
                if (command.startsWith("*")){
                    int numOfItems = Integer.parseInt(command.substring(1));
                    ArrayList<String> inputs = new ArrayList<>(numOfItems * 2);
                    for (int i =0; i < numOfItems * 2; i++){
                        inputs.add(reader.readLine());
                    }
                    String cmd = inputs.get(1);
                    switch (cmd.toLowerCase()) {
                        case Constants.CMD_PING ->
                                outputStream.write( new Ping().print(inputs, cache));
                        case Constants.CMD_ECHO ->
                                outputStream.write( new Echo().print(inputs,cache));
                        case Constants.CMD_SET ->{
                            System.out.println("entering in replica SET in mast");
                            outputStream.write(new Set().print(inputs, cache));
                        }
                        case Constants.CMD_GET ->
                                outputStream.write(new Get().print(inputs, cache));
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

     String randomId(){
        //32
        String uuid = UUID.randomUUID().toString().replace("-", "");
        String extra = "abcd1234";
        return uuid + extra;

    }
}
