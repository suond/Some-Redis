package server;

import utils.Utils;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.concurrent.Executors;

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
    @Override
    public void startServer(){
        executorService = Executors.newCachedThreadPool();
        try(ServerSocket serverSocket = new ServerSocket(this.port)) {
            serverSocket.setReuseAddress(true);

            while (!serverSocket.isClosed()) {
                try {
                    final Socket clientSocket = serverSocket.accept();
                    System.out.println("client info in redis slave startServer: " + clientSocket.toString());
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

    public void connectToMaster(){
        System.out.println(
                String.format("value of masterHost: %s, value of masterip: %s", masterHost,masterPort));
        try{
            Socket masterSocket = new Socket(this.masterHost, this.masterPort);
            PrintWriter writer = new PrintWriter(new OutputStreamWriter(masterSocket.getOutputStream()));
            String pingCmd = Utils.toRESP(new String[] {"PING"});
            String replCmd1 = Utils.toRESP(new String[] {"REPLCONF","listening-port", Integer.toString(this.port)});
            String replCmd2 = Utils.toRESP(new String[] {"REPLCONF","capa", "psync2"});
            String psyncCmd = Utils.toRESP(new String[] {"PSYNC", "?", "-1"});
            writer.print(pingCmd);
            writer.print(replCmd1);
            writer.print(replCmd2);
            writer.print(psyncCmd);
            writer.flush();
//            System.out.println("master socket info in connect to master:" + masterSocket);
        } catch (UnknownHostException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
