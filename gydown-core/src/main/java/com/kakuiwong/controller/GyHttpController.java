package com.kakuiwong.controller;

import com.kakuiwong.annotation.GyHttp;
import com.kakuiwong.cache.GyCache;
import com.kakuiwong.domain.GyHttpRequest;
import com.kakuiwong.domain.HttpResult;
import com.kakuiwong.enums.GyStatusEnum;
import com.kakuiwong.server.NettyServer;
import com.kakuiwong.service.DownloadService;
import com.kakuiwong.service.HttpDownloadService;
import com.kakuiwong.util.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

import java.util.Set;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class GyHttpController implements GyController {

    private static DownloadService downloadService = new HttpDownloadService();

    static {
        AllJobsStatusUtil.readJobs();
    }

    /**
     * http下载
     *
     * @param ctx
     * @param request
     */
    @Override
    @GyHttp("/httpDownload")
    public void startDownload(ChannelHandlerContext ctx, GyHttpRequest request) {
        //检测参数
        Long fileSize = GyHttpDownload.downloadStatus(request.getDownloadUrl());
        if (GyAssertUtil.checkParam(ctx, request, fileSize) == -1) {
            return;
        }

        ThreadPoolUtil.POOL.getPool().execute(() -> downloadService.startDownload(request, fileSize));

        //进行存储下载进度
        ChannelUtil.send(ctx, HttpResult.okJson("start httpDownload", request), HttpResponseStatus.OK);
    }

    /**
     * 停止下载任务
     *
     * @param ctx
     * @param request
     */
    @Override
    @GyHttp("/download-stop")
    public void downloadStop(ChannelHandlerContext ctx, GyHttpRequest request) {
        if (GyAssertUtil.checkParam(ctx, request) == -1) {
            return;
        }
        ThreadPoolUtil.POOL.getPool().execute(() -> {
            downloadService.stopDownload(request);
            AllJobsStatusUtil.writeAllJobFile(GyCache.allJobsMap);
        });
        ChannelUtil.send(ctx, HttpResult.okJson("stop download", request), HttpResponseStatus.OK);
    }

    /**
     * 删除下载任务
     *
     * @param ctx
     * @param request
     */
    @Override
    @GyHttp("/download-delete")
    public void downloadDelete(ChannelHandlerContext ctx, GyHttpRequest request) {
        if (GyAssertUtil.checkParam(ctx, request) == -1) {
            return;
        }
        ThreadPoolUtil.POOL.getPool().execute(() -> downloadService.deleteDownload(request));
        ChannelUtil.send(ctx, HttpResult.okJson("delete download", request), HttpResponseStatus.OK);
    }

    /**
     * 关闭netty线程
     *
     * @param ctx
     * @param request
     */
    @Override
    @GyHttp("/download-shutdown")
    public void downloadShutdown(ChannelHandlerContext ctx, GyHttpRequest request) {
        Set<String> keys = GyCache.threadMap.keySet();
        if (keys != null && !keys.isEmpty()) {
            keys.stream().forEach(key -> {
                AllJobsStatusUtil.setJobStatus(key, GyStatusEnum.STOP.getStatus(), false);
            });
            GyCache.pubAllJobs();
        }
        ChannelUtil.send(ctx, HttpResult.okJson("shutdown", request), HttpResponseStatus.OK);
        NettyServer.getInstance().stopServerChannel();
        ThreadPoolUtil.POOL.getPool().shutdown();
    }
}
