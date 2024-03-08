import org.junit.Test;
import top.ninng.common.utils.Yaml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author OhmLaw
 * @Date 2024/1/28 12:31
 * @Version 1.0
 */
public class Config {

    @Test
    public void config() {
        Map<String, Object> item = new HashMap<>();
        item.put("server-port", 9980);
        item.put("client-port", 8080);
        item.put("proxy-type", "tcp");
        item.put("description", "http代理");
        ArrayList<Map<String, Object>> list = new ArrayList<>();
        list.add(item);

        Map<String, Object> config = new HashMap<>();
        config.put("localhost", "127.0.0.1");
        config.put("port", 8081);
        config.put("client-key", "client-key");
        config.put("server-host", "127.0.0.1");
        config.put("server-port", 8081);
        config.put("mapping", list);
        System.out.println(Yaml.dump(config));
    }
}
