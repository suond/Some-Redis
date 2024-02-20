package command;

import java.util.List;
import java.util.Map;

public class Get implements Command{
    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
        String key = inputs.get(3);

        String value = cache.get(key);

//        System.out.println("get: " + key + " gets: " + value);

        String result = value == null ? "$-1\r\n" : "+" + value + "\r\n";

        return result.getBytes();
    }
}
