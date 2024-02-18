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
        option = Option.builder("replicaof")
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
                server = new RedisSlave();
                server.setRole("slave");
                String[] relicaofCmds = cmd.getOptionValues("replicaof");
                server.setMasterHost(relicaofCmds[0]);
                ((RedisSlave)server).connectToMaster();
            } else {
                server = new RedisMaster();
                server.setRole("master");
            }
            System.out.println("bool val: " + cmd.hasOption("port"));
            if (cmd.hasOption("port")){
                int p = Integer.parseInt(cmd.getOptionValue("port"));
                System.out.println("port val is " + p);
                server.setPort(p);
            }

        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        server.startServer();
    }
}
