package com.kakuiwong.config;

import com.kakuiwong.contant.HttpPathContant;
import com.kakuiwong.domain.CoreYmlConfig;
import com.kakuiwong.util.YmlReader;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class ConfigInit {

    public static CoreYmlConfig readConfig() throws IOException {
        CoreYmlConfig config = new CoreYmlConfig();
        String configPath = System.getProperty("user.dir") + File.separator +
                HttpPathContant.CONFIG_PATH + File.separator + "app.yml";
        try {
            YmlReader.readYml(new FileInputStream(configPath), config);
        } catch (Exception e) {
            System.err.println("加载默认配置");
            InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("app.yml");
            File file = new File(System.getProperty("user.dir") + File.separator +
                    HttpPathContant.CONFIG_PATH);
            byte[] b = null;
            if (!file.exists()) {
                file.mkdirs();
                b = new byte[inputStream.available()];
                inputStream.read(b);
                Files.write(Paths.get(configPath), b);
            }
            YmlReader.readYml(new ByteArrayInputStream(b), config);
        }
        return config;
    }
}
