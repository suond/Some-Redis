package command;

import java.util.List;
import java.util.Map;

public class Set implements Command{
    @Override
    public byte[] print(List<String> strings, Map<String, String> cache) {
        String key = strings.get(3);
        String value = strings.get(5);

        cache.put(key, value);

        String result = "+OK\r\n";

        return result.getBytes();
    }
}
