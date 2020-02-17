package com.kakuiwong.util;


import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.util.StatusPrinter;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class LogBackUtil {

    public static void load() {
        InputStream resourceAsStream = LogBackUtil.class.getClassLoader().getResourceAsStream("logback.xml");
        LoggerContext lc = (LoggerContext) LoggerFactory.getILoggerFactory();
        try {
            if (resourceAsStream == null || resourceAsStream.available() < 1) {
                throw new IOException("Logback External Config File Parameter exists, but does not reference a file");
            } else {
                JoranConfigurator configurator = new JoranConfigurator();
                configurator.setContext(lc);
                lc.reset();
                configurator.doConfigure(resourceAsStream);
                StatusPrinter.printInCaseOfErrorsOrWarnings(lc);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
