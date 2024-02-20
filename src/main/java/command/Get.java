package command;

import server.Redis;

import java.util.List;
import java.util.Map;

public class Get implements Command{
    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
        inputs.stream().forEach(System.out::println);
        String key = inputs.get(3);

        String value = cache.get(key);
//"$-1\r\n"
        String result = value == null ? "123\r\n" : "+" + value + "\r\n";

        System.out.println("ret: " + result);

        return result.getBytes();
    }

    public byte[] printWithLoggingType(Redis redis, List<String> inputs, Map<String, String> cache){
        System.out.println(redis.getClass() + ", role: " + redis.getRole());

        return print(inputs,cache);
    }
}
