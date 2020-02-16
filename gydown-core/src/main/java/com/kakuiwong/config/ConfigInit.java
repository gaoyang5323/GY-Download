package com.kakuiwong.config;

import com.kakuiwong.domain.CoreYmlConfig;
import com.kakuiwong.util.YmlReader;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class ConfigInit {

    public static CoreYmlConfig readConfig() {
        CoreYmlConfig config = new CoreYmlConfig();
        try {
            YmlReader.readYml(new FileInputStream(System.getProperty("user.dir") + File.separator + "app.yml"), config);
        } catch (Exception e) {
            System.err.println("加载默认配置");
            YmlReader.readYml(Thread.currentThread().getContextClassLoader().getResourceAsStream("app.yml"), config);
        }
        return config;
    }
}
