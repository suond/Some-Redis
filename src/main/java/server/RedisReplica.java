package server;

import constants.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

public class RedisReplica extends Redis{
    String masterHost;
    int masterIp;

    public RedisReplica(String[] args){
        masterReplid = super.randomId();
        setArguments(args);
        super.startServer();
    }
    void setArguments(String[] args){
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

    public void startServer(){
        executorService = Executors.newCachedThreadPool();
        connectToMaster();
    }

    public void connectToMaster(){
        try{
            System.out.println("masterHost: " + this.masterHost + " , masterIp: " + this.masterIp);
            Socket mainSocket = new Socket(this.masterHost, this.masterIp);
            OutputStream writer = mainSocket.getOutputStream();
            String ping = "*1"+ Constants.R_N + "$4"+ Constants.R_N+ "PING" + Constants.R_N;
            writer.write(ping.getBytes());
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
