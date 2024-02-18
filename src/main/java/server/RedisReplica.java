package server;

import constants.Constants;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import java.net.UnknownHostException;

public class RedisReplica extends Redis{
    String masterHost;
    int masterIp;

    public RedisReplica(String[] args){
        super(args);
    }

    public void connectToMaster(){
        try{
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
