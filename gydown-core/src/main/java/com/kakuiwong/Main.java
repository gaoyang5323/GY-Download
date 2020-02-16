package com.kakuiwong;

import com.kakuiwong.config.ConfigInit;
import com.kakuiwong.config.LogInit;
import com.kakuiwong.domain.CoreYmlConfig;
import com.kakuiwong.server.NettyServer;
import com.kakuiwong.util.ThreadPoolUtil;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class Main {

    public static CoreYmlConfig config;

    public static void main(String[] args) {
        try {
            addShutdownHook();

            config = ConfigInit.readConfig();

            LogInit.loadLog(config);

            ThreadPoolUtil.POOL.getPool().execute(() -> {
                try {
                    NettyServer.getInstance().start(config);
                } catch (Exception e) {
                    e.printStackTrace();
                } finally {
                    NettyServer.getInstance().stop();
                }
            });

            systemOutStart();
        } catch (Exception e) {
            e.printStackTrace();
            System.exit(-1);
        }
    }


    private static void systemOutStart() {
        System.out.println(
                "  _______     __     _____   ______          ___   _ _      ____          _____  \n" +
                        " / ____\\ \\   / /    |  __ \\ / __ \\ \\        / / \\ | | |    / __ \\   /\\   |  __ \\ \n" +
                        "| |  __ \\ \\_/ /_____| |  | | |  | \\ \\  /\\  / /|  \\| | |   | |  | | /  \\  | |  | |\n" +
                        "| | |_ | \\   /______| |  | | |  | |\\ \\/  \\/ / | . ` | |   | |  | |/ /\\ \\ | |  | |\n" +
                        "| |__| |  | |       | |__| | |__| | \\  /\\  /  | |\\  | |___| |__| / ____ \\| |__| |\n" +
                        " \\_____|  |_|       |_____/ \\____/   \\/  \\/   |_| \\_|______\\____/_/    \\_\\_____/ \n");
        System.out.println("开启监听端口:" + config.getPort());
        System.out.println("存储位置:" + config.getDownloadPath());
    }

    private static void addShutdownHook() {
        Runtime.getRuntime().addShutdownHook(new Thread(() -> System.out.println("GyDown shutdown")));
    }
}
