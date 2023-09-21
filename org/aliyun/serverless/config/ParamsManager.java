package org.aliyun.serverless.config;

import org.yaml.snakeyaml.Yaml;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.util.Map;
import java.util.logging.Logger;

public class ParamsManager {
    private static final Logger logger = Logger.getLogger(ParamsManager.class.getName());
//    // prod
//    private static final String CONFIG_FILE_PATH = "/app/config.yaml";
//    // debug
//    private static final String CONFIG_FILE_PATH = "/Users/yeyuan/CrazyScaler/java/config.yaml";
    private Map<String, Object> params = null;
    private static ParamsManager pm = new ParamsManager();

//    public static void main(String[] args) {
//        ParamsManager pm = new ParamsManager();
//        System.out.println(pm.getParamValue("fakeParams.fakeParamObject.subObject2"));
//    }

    private ParamsManager() {
        params = loadParams("/app/config.yaml");
//        params = loadParams("/Users/yeyuan/CrazyScaler/java/config.yaml");
    }

    public static ParamsManager getManager() {
        return pm;
    }

    public Object getParamValue(String key) {
        Object resParam = getParamValueRecursively(this.params, key);
        if (null == resParam) {
            logger.warning(String.format("Param %s is null", key));
        }
        return resParam;
    }

    /**
     * 根据全限定Key获取嵌套字典中的Value
     * 全限定Key形如"key1.subKey2.subSubKey3"
     * @param key
     * @return
     */
    @SuppressWarnings("unchecked")
    private Object getParamValueRecursively(Map<String, Object> map, String key) {
        // recursively traverse the map using "." as delimiter
        String[] keys = key.split("\\.");

        if (null == map || 0 == keys.length) {
            return null;
        }

        if (1 == keys.length) {
            return map.get(keys[0]);
        }

        Object value = map.get(keys[0]);
        // 确定value是字典, 才能继续递归
        if (value instanceof Map) {
            return getParamValueRecursively((Map<String, Object>) value,
                    key.substring(keys[0].length() + 1));
        }

        return null;
    }

    /**
     *
     * @param yamlFilePath
     * @return
     */
    private Map<String, Object> loadParams(String yamlFilePath) {
        try {
            Yaml yaml = new Yaml();
            FileInputStream inputStream = new FileInputStream(yamlFilePath);

            return yaml.load(inputStream);
        } catch (FileNotFoundException e) {
            logger.warning("File not found: " + yamlFilePath);
            throw new RuntimeException("File not found: " + yamlFilePath, e);
        }
    }

}
