package com.kakuiwong.service;

import com.kakuiwong.cache.GyCache;
import com.kakuiwong.domain.DownloadProperties;
import com.kakuiwong.domain.GyHttpRequest;
import com.kakuiwong.enums.GyStatusEnum;
import com.kakuiwong.util.AllJobsStatusUtil;
import com.kakuiwong.util.GyHttpDownload;
import com.kakuiwong.util.ThreadPoolUtil;
import io.netty.util.internal.ThreadLocalRandom;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.concurrent.ThreadPoolExecutor;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class HttpDownloadService implements DownloadService {

    private static Logger log = LoggerFactory.getLogger(HttpDownloadService.class);

    @Override
    public void startDownload(GyHttpRequest request, Long fileSize) {
        //获取要下载地址及保存路径
        String downloadUrl = request.getDownloadUrl();
        String realDownloadPath = getReadDownPath(request);

        //查询是否为已存在下载任务,通过配置文件拿到下载进度
        long point = 0L;
        String tempDownloadPath = realDownloadPath + ".gy";
        String downloadPropPath = realDownloadPath + ".gyp";
        File tempFile = new File(tempDownloadPath);
        if (tempFile.isFile()) {
            //读取temp文件的信息
            DownloadProperties downloadProperties = GyHttpDownload.readTempFile(downloadPropPath);
            if (downloadProperties != null) {
                point = downloadProperties.getPoint();
            }
        }

        GyCache.pubAllJobs();
        AllJobsStatusUtil.createJob(request);
        AllJobsStatusUtil.setJobStatus(downloadUrl, GyStatusEnum.START.getStatus(), true);

        //进行下载
        ThreadPoolExecutor pool = ThreadPoolUtil.POOL.getPool();
        long realPoint = point;
        pool.execute(() -> {
            GyCache.threadMap.put(downloadUrl, Thread.currentThread());
            GyCache.addThreadAliveMap(downloadUrl);
            try {
                //开始下载,保存为临时文件
                GyHttpDownload.download(tempDownloadPath, downloadUrl, realPoint, downloadPropPath, fileSize);
                //下载完成清除任务,并且删除指针文件及更改后缀
                deleteMap(downloadUrl);
                File downloadPropFile = new File(downloadPropPath);
                downloadPropFile.delete();
                tempFile.renameTo(new File(realDownloadPath));
            } catch (Exception e) {
                //任务暂停进行操作
                log.info("暂停 " + downloadUrl);
            }
        });
    }

    @Override
    public void stopDownload(GyHttpRequest request) {
        Thread thread = GyCache.threadMap.get(request.getDownloadUrl());
        if (thread != null) {
            GyCache.threadAliveMap.remove(request.getDownloadUrl());
            GyCache.threadMap.remove(request.getDownloadUrl());
            AllJobsStatusUtil.setJobStatus(request.getDownloadUrl(), GyStatusEnum.STOP.getStatus(), false);
            GyCache.pubAllJobs();
        }
    }

    @Override
    public void deleteDownload(GyHttpRequest request) {
        stopDownload(request);
        deleteMap(request.getDownloadUrl());
        AllJobsStatusUtil.writeAllJobFile(GyCache.allJobsMap);
        File tempDownloadFile = new File(getReadDownPath(request) + ".gy");
        File downloadPropFile = new File(getReadDownPath(request) + ".gyp");
        if (tempDownloadFile.isFile()) {
            deleteFile(tempDownloadFile);
        }
        if (downloadPropFile.isFile()) {
            deleteFile(downloadPropFile);
        }
        GyCache.pubAllJobs();
    }

    private void deleteFile(File file) {
        int count = 0;
        boolean delete = false;
        do {
            delete = file.delete();
            count++;
            try {
                Thread.sleep(ThreadLocalRandom.current().nextInt(100, 1000));
            } catch (InterruptedException e) {
                log.error("deleteFile", e);
            }
        } while (!delete && count < 5);
    }

    private String getReadDownPath(GyHttpRequest request) {
        return request.getDownloadPath()
                + File.separator
                + request.getDownloadUrl().substring(request.getDownloadUrl().lastIndexOf("/") + 1);
    }


    private void deleteMap(String downloadUrl) {
        GyCache.threadMap.remove(downloadUrl);
        GyCache.allJobsMap.remove(downloadUrl);
        GyCache.threadAliveMap.remove(downloadUrl);
    }
}
