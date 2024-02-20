package command;

import java.util.List;
import java.util.Map;

public class ReplConf implements Command{
    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
        String result = "+OK\r\n";
        return result.getBytes();
    }
}
