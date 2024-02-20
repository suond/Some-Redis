import org.apache.commons.cli.*;
import server.Redis;
import server.RedisMaster;
import server.RedisSlave;

public class RedisStarter {

    public static void start(String[] args){
        Options options = new Options();
        Option option = Option.builder("port")
                .longOpt("port")
                .hasArg()
                .desc("redis's port")
                .build();
        options.addOption(option);
        option = Option.builder("replica")
                .longOpt("replicaof")
                .numberOfArgs(2)
                .desc("server slave of appointed master and ip")
                .build();
        options.addOption(option);
        parseAndStart(options, args);
    }

    private static void parseAndStart(Options options, String[] args) {
        Redis server = null;
        CommandLineParser parsers = new DefaultParser();
        CommandLine cmd;

        try{
            cmd = parsers.parse(options, args);
            if (cmd.hasOption("replicaof")) {
//                System.out.println("going into replicaof");
                server = new RedisSlave();
                server.setRole("slave");
                String[] replicaof = cmd.getOptionValues("replicaof");
//                for (String s: replicaof)
//                    System.out.println(s);
                if (replicaof[0].equals("localhost")){
                    ((RedisSlave)server).setMasterHost("127.0.0.1");
                } else {
                    ((RedisSlave)server).setMasterHost(replicaof[0]);
                }
                ((RedisSlave)server).setMasterPort(Integer.parseInt(replicaof[1]));
//                ((RedisSlave)server).connectToMaster();
            } else {
                server = new RedisMaster();
                server.setRole("master");
            }
            if (cmd.hasOption("port")){
                int p = Integer.parseInt(cmd.getOptionValue("port"));
                server.setPort(p);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        if (server instanceof RedisSlave)
            ((RedisSlave) server).connectToMaster();
        server.startServer();
    }
}
