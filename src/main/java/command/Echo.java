package command;

import command.Command;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Echo implements Command {

    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
        String echoOut = "+"+inputs.get(3)+ "\r\n";
        return echoOut.getBytes();
    }
}
