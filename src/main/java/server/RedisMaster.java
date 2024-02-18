package server;

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

//    String randomId(){
//        //32
//        String uuid = UUID.randomUUID().toString().replace("-", "");
//        String extra = "abcd1234";
//        return uuid + extra;
//
//    }
}
