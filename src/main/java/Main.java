import server.Redis;
import server.RedisSlave;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.cli.*;

public class Main {
    static Map<String, String> cache = new HashMap<>();
  public static void main(String[] args) {

      // You can use print statements as follows for debugging, they'll be visible when running tests.
      System.out.println("Logs from your program will appear here!");

      if (args.length > 1 && args[0].equalsIgnoreCase("--port")){
          if (args.length > 2 && args[2].equals("--replicaof")){
              System.out.println("creating replica");
              RedisSlave redis = new RedisSlave(args);
          } else {
            Redis redis = new Redis(args);
          }
      } else {
          Redis redis = new Redis();
      }

  }
}
