package command;

import command.Command;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Echo implements Command {

    @Override
    public byte[] print(List<String> strings, Map<String, String> cache) {
        String echoOut = "+"+strings.get(3)+ "\r\n";
        return echoOut.getBytes();
    }
}
