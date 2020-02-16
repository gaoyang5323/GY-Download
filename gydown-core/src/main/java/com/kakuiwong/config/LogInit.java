package com.kakuiwong.config;

import com.kakuiwong.domain.CoreYmlConfig;
import com.kakuiwong.util.LogBackUtil;

import java.io.File;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class LogInit {

    public static void loadLog(CoreYmlConfig config) {
        System.setProperty("log-path", config.getDownloadPath() + File.separator + "gydown-log.log");
        LogBackUtil.load();
    }
}
