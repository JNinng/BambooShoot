package top.ninng.common.utils;

import org.yaml.snakeyaml.DumperOptions;

import java.io.InputStream;

/**
 * @Author OhmLaw
 * @Date 2024/1/28 12:18
 * @Version 1.0
 */
public class Yaml {

    private static org.yaml.snakeyaml.Yaml yaml;

    static {
        init();
    }

    public static String dump(Object data) {
        return yaml.dump(data);
    }

    public static org.yaml.snakeyaml.Yaml getYaml() {
        init();
        return yaml;
    }

    private static void init() {
        if (yaml == null) {
            DumperOptions dumperOptions = new DumperOptions();
            dumperOptions.setDefaultFlowStyle(DumperOptions.FlowStyle.BLOCK);
            dumperOptions.setDefaultScalarStyle(DumperOptions.ScalarStyle.PLAIN);
            yaml = new org.yaml.snakeyaml.Yaml(dumperOptions);
        }
    }

    public static Object load(InputStream io) {
        init();
        return yaml.load(io);
    }
}
