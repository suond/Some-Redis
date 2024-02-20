package server;

import command.*;
import constants.Constants;

import java.io.*;
import java.net.Socket;
import java.util.ArrayList;
import java.util.UUID;

public class RedisMaster extends Redis{

    public RedisMaster(){
        this.masterReplid = randomId();
        this.masterReplOffset = 0;
    }
    public String getMasterReplid() {
        return this.masterReplid;
    }

    public int getMasterReplOffset(){
        return this.masterReplOffset;
    }

    @Override
    void handle (Socket clientSocket){
        try (InputStream inputStream = clientSocket.getInputStream()){

            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            OutputStream outputStream = clientSocket.getOutputStream();

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
                        case Constants.CMD_REPLCONF ->
                            outputStream.write(new ReplConf().print(inputs,cache));
                        case Constants.CMD_PSYNC -> {
                            String output = String.format(
                                    "+FULLSYNC %s %s%s", this.masterReplid, this.masterReplOffset, Constants.R_N
                            );
                            outputStream.write(output.getBytes());
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
}
