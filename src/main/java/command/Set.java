package command;

import server.Redis;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Set implements Command{

    private static final ExecutorService thread = Executors.newSingleThreadExecutor();
    @Override
    public byte[] print(List<String> inputs, Map<String, String> cache) {
//        System.out.println("Inside set");
        System.out.println("entering in replica SET");
        String key = inputs.get(3);
        String value = inputs.get(5);

        if (inputs.size() > 6){
            //option = 7, duration = 9
            String option = inputs.get(7);
            if (option.toLowerCase().equals("px")){
                System.out.println("Duration in ms: " + inputs.get(9));
                Duration expiry = Duration.ofMillis(Long.parseLong(inputs.get(9)));

                thread.execute(() -> removeKey(key,cache,expiry));
            }
        }

        cache.put(key, value);
        String result = "+OK\r\n";
        return result.getBytes();
    }

    public byte[] print (Redis redis, List<String> inputs, Map<String, String> cache){
        System.out.println("Role of calling redis type: " + redis.getRole());
        return print(inputs, cache);
    }

    private void removeKey(String key, Map<String,String> cache, Duration expiry){
        try{
            Thread.sleep(expiry);
            cache.remove(key);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }
}
