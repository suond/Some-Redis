import command.Echo;
import command.Get;
import command.Ping;
import command.Set;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Redis {


    int port;
    ExecutorService executorService;
    Map<String, String> cache = new HashMap<>();
    String role = "master";
    String masterHost;
    int masterIp;

    public Redis( int port){
        this.port = port;
        startServer();
    }

    public Redis(){
        this.port = 6379;
        startServer();
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
                System.out.println("command: " + command);
                if (command.startsWith("*")){
                    int numOfItems = Integer.parseInt(command.substring(1));
                    //0 = size of 1, 1 = cmd, 2 = size of 3, 3 = key/echo val, 4 = size of set, 5 = setVal
                    ArrayList<String> inputs = new ArrayList<>(numOfItems * 2);
                    for (int i =0; i < numOfItems * 2; i++){
                        inputs.add(reader.readLine());
                        System.out.println("element " + i + " is: " + inputs.get(i));
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
                                System.out.println("port: " + port + ", role: " + role);
                                String r = "role:" + this.role;
                                String output = "$"+r.length() + "\r\n" + r + "\r\n";
                                outputStream.write(output.getBytes());
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
}
