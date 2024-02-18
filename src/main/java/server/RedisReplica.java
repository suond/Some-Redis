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
        super();
    }

}
