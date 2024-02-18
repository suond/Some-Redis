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

    int port;
    ExecutorService executorService;
    Map<String, String> cache = new HashMap<>();
    String role = "master";
    String masterHost;
    int masterIp;
    String masterReplid;
    int masterReplOffset = 0;


    public Redis( String[] args){
        masterReplid = randomId();
        setArguments(args);
        startServer();
    }

    private void setArguments(String[] args) {
        if (args == null) return;
        for (int i = 0; i < args.length; i++){
            if (args[i].equals("--port")){
                try{
                    this.port = Integer.parseInt(args[i+1]);
                } catch (NumberFormatException e){
                    System.out.println("can't parse number");
                    System.exit(1);
                }
            }
            if (args[i].equals("--replicaof") && i+2 < args.length){
                String masterHost = args[i+1];
                setMasterHost(masterHost);
                try{
                    int masterIp = Integer.parseInt(args[i+2]);
                    setMasterIp(masterIp);
                } catch (NumberFormatException e){
                    System.out.println("can't parse number");
                    System.exit(1);
                }
                setRole("slave");
            }
        }
    }

    public Redis(){
        masterReplid = randomId();
        this.port = 6379;
        startServer();
    }

    public int getPort(){
        return this.port;
    }

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

    public void setMasterHost(String masterHost){
        this.masterHost = masterHost;
    }
    public void setMasterIp(int ip){
        this.masterIp = ip;
    }

    private void startServer(){
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

     private void handle (Socket clientSocket){
        try (InputStream inputStream = clientSocket.getInputStream()){

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = clientSocket.getOutputStream();
//              PrintWriter writer = new PrintWriter(clientSocket.getOutputStream(), true);

            String command;
            while ((command = reader.readLine()) != null) {
//                System.out.println("command: " + command);
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
                        case Constants.CMD_PING ->
                                outputStream.write( new Ping().print(inputs, cache));
                        case Constants.CMD_ECHO ->
                                outputStream.write( new Echo().print(inputs,cache));
                        case Constants.CMD_SET ->
                                outputStream.write(new Set().print(inputs, cache));
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

    private String randomId(){
        //32
        String uuid = UUID.randomUUID().toString().replace("-", "");

        String extra = "abcd1234";
        return uuid + extra;

    }
}
