package server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class RedisSlave extends Redis{
    String masterHost;
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

    public void connectToMaster(){
        System.out.println(
                String.format("value of masterHost: %s, value of masterip: %s", masterHost,masterPort));
        try{
            Socket masterSocket = new Socket(this.masterHost, this.masterPort);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(masterSocket.getOutputStream()));
            String pingCmd = "*1\r\n$4\r\nPING\r\n";
            writer.print(pingCmd);
            writer.flush();
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
