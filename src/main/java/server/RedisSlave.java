package server;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.UnknownHostException;

public class RedisSlave extends Redis{
    String masterHost;
    int masterIp;
    String role = "slave";
    int MasterPort;

    public RedisSlave(){
        super();
    }

    public void connectToMaster(){
        try{
            Socket masterSocket = new Socket(this.masterHost, this.masterIp);
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
