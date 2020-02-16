package com.kakuiwong.cache;

import com.alibaba.fastjson.JSON;
import com.kakuiwong.domain.AllJobsProperties;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class GyCache {

    //websocket
    public static List<ChannelHandlerContext> channelList = new CopyOnWriteArrayList<>();
    //各任务线程用于中止下载
    public final static Map<String, Thread> threadMap = new ConcurrentHashMap<>();
    //存活线程
    public final static Map<String, Object> threadAliveMap = new ConcurrentHashMap<>();
    private final static Object obj = new Object();
    //所有任务信息
    public final static Map<String, AllJobsProperties> allJobsMap = new ConcurrentHashMap<>();

    public static void addThreadAliveMap(String str) {
        threadAliveMap.put(str, obj);
    }

    public static void pubAllJobs() {
        if (channelList.isEmpty()) {
            return;
        }
        channelList.stream().forEach(channel -> {
            Collection<AllJobsProperties> values = allJobsMap.values();
            try {
                channel.writeAndFlush(new TextWebSocketFrame()
                        .replace(Unpooled.copiedBuffer(JSON.toJSONString(values)
                                .getBytes("utf-8"))));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        });
    }

    public static void putAllJobMapByWork(String key, AllJobsProperties value
            , AllJobInterface allJobInterface) {
        allJobsMap.put(key, value);
        allJobInterface.work(allJobsMap);
    }

    public static void removeAllJobMapByWork(String key, AllJobInterface allJobInterface) {
        allJobsMap.remove(key);
        allJobInterface.work(allJobsMap);
    }

    @FunctionalInterface
    public interface AllJobInterface {
        void work(Map<String, AllJobsProperties> allJobsMap);
    }
}
