package command;

import server.Redis;

import java.util.List;
import java.util.Map;

public class Info implements Command{

    Redis redis;
    public Info (Redis redis){
        this.redis = redis;
    }

    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
        System.out.println("port: " + redis.getPort() + ", role: " + redis.getRole());
        String r = "role:" + redis.getRole() + "\r\n"
                + "master_replid:" + redis.getMasterReplid() + "\r\n"
                + "master_reol_offset:" + redis.getMasterReplOffset() + "\r\n";
//        String output = "$"+r.length() + "\r\n" + r + "\r\n"
        return bulk(r).getBytes();
    }

    private String bulk(String s){
        return "$"+ s.length() + "\r\n" + s + "\r\n";
    }
}
