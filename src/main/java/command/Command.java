package command;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface Command {

    public byte[] print(List<String> inputs, Map<String, String> cache);
}
