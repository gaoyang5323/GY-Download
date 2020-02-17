package com.kakuiwong.util;

import com.alibaba.fastjson.JSON;
import com.kakuiwong.cache.GyCache;
import com.kakuiwong.contant.HttpPathContant;
import com.kakuiwong.domain.AllJobsProperties;
import com.kakuiwong.domain.GyHttpRequest;
import com.kakuiwong.enums.GyStatusEnum;
import io.netty.util.internal.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.FileLock;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class AllJobsStatusUtil {
    private static Logger log = LoggerFactory.getLogger(AllJobsStatusUtil.class);
    private static final String ALL_JOB_FILE_PATH = System.getProperty("user.dir") +
            File.separator + HttpPathContant.CONFIG_PATH + File.separator + "jobs.json";

    public static void readJobs() {

        File file = new File(ALL_JOB_FILE_PATH);
        if (!file.exists()) {
            return;
        }
        List<AllJobsProperties> allList;
        if (file.isFile()) {
            byte[] bytes = null;
            try {
                bytes = Files.readAllBytes(Paths.get(ALL_JOB_FILE_PATH));

                allList = JSON.parseArray(new String(bytes, Charset.forName("UTF-8")), AllJobsProperties.class);
                allList.stream().forEach(job -> {
                    GyCache.allJobsMap.put(job.getDownloadUrl(), job);
                });
            } catch (Exception e) {
                // log.error("readJobs",e);
            }
        }
    }

    public static void setPercentagePointAndDownloadSpeed(String url, Long filePointer, Long allSize, double speed) {
        AllJobsProperties allJobsProperties = GyCache.allJobsMap.get(url);
        Optional.ofNullable(allJobsProperties).ifPresent(job -> {
            job.setPercentagePoint(getPercentagePoint(filePointer, allSize));
            job.setDownLoadSpeed(speed);
        });
        writeAllJobFile(GyCache.allJobsMap);
    }

    public static void setJobStatus(String url, Integer status, boolean isWrite) {
        AllJobsProperties allJobsProperties = GyCache.allJobsMap.get(url);
        Optional.ofNullable(allJobsProperties).ifPresent(job -> {
            job.setStatus(status);
        });
        if (isWrite) {
            writeAllJobFile(GyCache.allJobsMap);
        }
    }

    public static void createJob(GyHttpRequest request) {
        AllJobsProperties allJobsProperties = GyCache.allJobsMap.get(request.getDownloadUrl());
        if (allJobsProperties != null) {
            return;
        }
        allJobsProperties = new AllJobsProperties(request.getDownloadUrl()
                , request.getDownloadPath()
                , 0D
                , GyStatusEnum.START.getStatus()
                , 0D);
        putAllJobMap(request.getDownloadUrl(), allJobsProperties);
    }

    public static void putAllJobMap(String key, AllJobsProperties value) {
        GyCache.putAllJobMapByWork(key, value, map -> writeAllJobFile(map));
    }

    public static void writeAllJobFile(Map<String, AllJobsProperties> map) {
        File file = new File(ALL_JOB_FILE_PATH);
        try {
            if (!file.exists() && !file.createNewFile()) {
                return;
            }
        } catch (IOException e) {
            e.printStackTrace();
            log.error("writeAllJobFile", e);
        }

        RandomAccessFile randomAccessFile = null;
        FileChannel channel = null;
        FileLock fileLock = null;
        try {
            randomAccessFile = new RandomAccessFile(new File(ALL_JOB_FILE_PATH), "rw");
            channel = randomAccessFile.getChannel();
            while (fileLock == null) {
                try {
                    fileLock = channel.tryLock();
                } catch (Exception e) {
                    Thread.sleep(ThreadLocalRandom.current().nextInt(10, 100));
                }
            }
            Collection<AllJobsProperties> values = map.values();
            if (values.isEmpty()) {
                channel.truncate(0);
                channel.write(ByteBuffer.wrap("".getBytes("UTF-8")));
                return;
            }
            byte[] bytes = JSON.toJSONString(values).getBytes("UTF-8");
            channel.truncate(bytes.length);
            channel.write(ByteBuffer.wrap(bytes));
        } catch (Exception e) {
            log.error("writeAllJobFile", e);
        } finally {
            if (fileLock != null) {
                try {
                    fileLock.release();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (channel != null) {
                try {
                    channel.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if (randomAccessFile != null) {
                try {
                    randomAccessFile.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static double getPercentagePoint(Long realPoint, Long fileSize) {
        BigDecimal divide = BigDecimal.valueOf(realPoint)
                .divide(BigDecimal.valueOf(fileSize), 3, BigDecimal.ROUND_HALF_DOWN);
        divide = divide.multiply(BigDecimal.valueOf(100)).setScale(1, BigDecimal.ROUND_HALF_DOWN);
        return divide == null ? 0.0D : divide.doubleValue();
    }
}
