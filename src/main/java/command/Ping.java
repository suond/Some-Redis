package command;

import command.Command;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public class Ping implements Command {

    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
        System.out.println("hit ping");
        return "+PONG\r\n".getBytes();
    }
}
