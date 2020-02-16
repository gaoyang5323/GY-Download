package com.kakuiwong.util;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class LogBackUtil {

    public static void load() {
        String file = LogBackUtil.class.getClassLoader().getResource("logback.xml").getFile();
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            File externalConfigFile = new File(file);
            if (!externalConfigFile.exists()) {
                throw new IOException("Logback External Config File Parameter does not reference a file that exists");
            } else {
                if (!externalConfigFile.isFile()) {
                    throw new IOException("Logback External Config File Parameter exists, but does not reference a file");
                } else {
                    if (!externalConfigFile.canRead()) {
                        throw new IOException("Logback External Config File exists and is a file, but cannot be read.");
                    } else {
                        JoranConfigurator configurator = new JoranConfigurator();
                        configurator.setContext(lc);
                        lc.reset();
                        configurator.doConfigure(file);
                        StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
