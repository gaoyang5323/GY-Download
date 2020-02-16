package com.kakuiwong.util;

import com.alibaba.fastjson.JSON;
import com.kakuiwong.cache.GyCache;
import com.kakuiwong.domain.DownloadProperties;
import com.kakuiwong.domain.GydownException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class GyHttpDownload {


    private static Logger log = LoggerFactory.getLogger(GyHttpDownload.class);

    /**
     * 读取下载文件信息
     *
     * @param url
     * @return
     */
    public static Long downloadStatus(String url) {
        HttpURLConnection connection = null;
        if (url == null) {
            return 0L;
        }
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            if (connection.getResponseCode() != 200 && connection.getResponseCode() != 206) {
                return 0L;
            }
            return connection.getContentLengthLong();
        } catch (Exception e) {
            log.info("downloadStatus", e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }
        return 0L;
    }

    /**
     * 下载文件
     *
     * @param filePath
     * @param url
     * @param point
     * @param downloadPropPath
     * @param allSize
     */
    public static void download(String filePath, String url, long point, String downloadPropPath, Long allSize) {
        HttpURLConnection connection = null;
        RandomAccessFile fileRandom = null;
        RandomAccessFile propRandom = null;
        try {
            connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestMethod("GET");
            connection.setConnectTimeout(3000);
            connection.setRequestProperty("Range", "bytes=" + point + "-");//("Range", "bytes=" + point+ "-" + end);
            if (connection.getResponseCode() != 200 && connection.getResponseCode() != 206) {
                return;
            }
            fileRandom = new RandomAccessFile(new File(filePath), "rwd");
            fileRandom.setLength(allSize);
            fileRandom.seek(point);

            //写文件属性
            propRandom = new RandomAccessFile(new File(downloadPropPath), "rwd");
            DownloadProperties downloadProperties = new DownloadProperties();
            downloadProperties.setDownloadUrl(url);
            downloadProperties.setPoint(point);
            propRandom.write(strToBytes(downloadProperties));

            InputStream inputStream = connection.getInputStream();
            byte[] b = new byte[1024 * 1024 * 100];
            int read = 0;
            long start[] = {System.nanoTime()};
            long sumRead[] = {0};
            while (GyCache.threadAliveMap.get(url) != null
                    && (read = inputStream.read(b)) != -1) {

                fileRandom.write(b, 0, read);
                long filePointer = fileRandom.getFilePointer();
                downloadProperties.setPoint(filePointer);
                propRandom.seek(0);
                propRandom.write(strToBytes(downloadProperties));


                calDownloadSpeedAndDoSomethings(sumRead, start, url, filePointer, allSize);
                sumRead[0] += read;
            }
            if (GyCache.threadAliveMap.get(url) == null) {
                throw new GydownException(2333, "download stop");
            }
        } catch (IOException e) {
            log.error("download", e);
        } finally {
            close(connection, fileRandom, propRandom);
        }
    }

    /**
     * 读取下载文件属性信息
     *
     * @param downloadPropPath
     * @return
     */
    public static DownloadProperties readTempFile(String downloadPropPath) {
        DownloadProperties downloadProperties = null;
        File file = new File(downloadPropPath);
        if (!file.isFile()) {
            return DownloadProperties.empty();
        }
        try {
            byte[] bytes = Files.readAllBytes(Paths.get(downloadPropPath));
            String propertiesStr = new String(bytes, Charset.forName("UTF-8"));
            downloadProperties = JSON.parseObject(propertiesStr, DownloadProperties.class);
        } catch (IOException e) {
            log.info("readTempFile", e);
            return DownloadProperties.empty();
        }
        return downloadProperties;
    }

    private static void close(HttpURLConnection connection,
                              RandomAccessFile fileRandom,
                              RandomAccessFile propRandom) {
        if (connection != null) {
            connection.disconnect();
        }
        if (fileRandom != null) {
            try {
                fileRandom.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        if (propRandom != null) {
            try {
                propRandom.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static byte[] strToBytes(DownloadProperties downloadProperties) {
        return JSON.toJSONString(downloadProperties).getBytes(Charset.forName("UTF-8"));
    }

    private static void calDownloadSpeedAndDoSomethings(long[] sumRead, long[] start, String url
            , long filePointer, long allSize) {
        long end = System.nanoTime();
        if (sumRead[0] > 1024 * 1024 * 10) {
            double readM2 = (sumRead[0] / (1024.0 * 1024.0));
            double seconds2 = (double) (end - start[0]) / 1000000000.0;
            start[0] = end;
            sumRead[0] = 0;
            BigDecimal speed = new BigDecimal(readM2 / seconds2).setScale(2, RoundingMode.HALF_DOWN);

            AllJobsStatusUtil.setPercentagePointAndDownloadSpeed(url, filePointer, allSize, speed.doubleValue());
            GyCache.pubAllJobs();
        }
    }
}
