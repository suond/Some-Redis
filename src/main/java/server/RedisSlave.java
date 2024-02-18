package server;

public class RedisSlave extends Redis{
    String masterHost;
    int masterIp;
    String role = "slave";
    int MasterPort;

    public RedisSlave(String[] args){
        super();
    }

}
