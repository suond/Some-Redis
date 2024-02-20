package command;

import constants.Constants;

import java.util.List;
import java.util.Map;

public class Psync implements Command{
    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
        return new byte[0];
    }

    public byte[] print(String masterReplid, String masterReplOffset){
        String output = String.format(
                "+FULLRESYNC %s %s%s", masterReplid, masterReplOffset, Constants.R_N
        );
        return output.getBytes();
    }
}
