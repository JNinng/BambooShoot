package top.ninng.client;

import top.ninng.common.constant.C;
import top.ninng.common.utils.Yaml;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Map;

/**
 * 配置读取类
 *
 * @Author OhmLaw
 * @Date 2024/1/28 12:08
 * @Version 1.0
 */
public class Config {

    private static final String CONFIG_FILE_NAME = "bamboo-shoot-client.yaml";
    private static Map<String, Object> config;
    private static ArrayList<Map<String, Object>> portList;

    public Config() {
        init();
    }

    public static Object get(String key) {
        if (config == null) {
            init();
        }
        return config.get(key);
    }

    /**
     * 优先检测自定义配置，不存在则读取默认配置
     *
     * @return
     */
    private static File getConfigFile() {
        File config = getUserConfigFile();
        if (config == null || !config.exists() || config.length() <= 0) {
            config = getDefaultConfigFile();
        }
        return config;
    }

    /**
     * 获取 jar 内默认配置文件
     *
     * @return
     */
    private static File getDefaultConfigFile() {
        return new File(Config.class.getClassLoader().getResource(CONFIG_FILE_NAME).getPath());
    }

    public static ArrayList<Map<String, Object>> getPortMappingList() {
        if (portList == null || portList.size() <= 0) {
            init();
        }
        return portList;
    }

    /**
     * 获取运行 jar 目录下的自定义配置文件
     *
     * @return
     */
    private static File getUserConfigFile() {
        // 定位所在目录
        String dir = System.getProperty("user.dir");
        File file = new File(dir + File.separator + CONFIG_FILE_NAME);
        if (!file.exists()) {
            file = null;
        }
        return file;
    }

    public static void init() {
        File configFile = getConfigFile();
        try (InputStream inputStream = new FileInputStream(configFile)) {
            config = (Map<String, Object>) Yaml.load(inputStream);
            portList = (ArrayList<Map<String, Object>>) config.get(C.PORT_MAPPING);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
