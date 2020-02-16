package com.kakuiwong.util;

import com.kakuiwong.Main;
import com.kakuiwong.cache.GyCache;
import com.kakuiwong.domain.GyHttpRequest;
import com.kakuiwong.domain.HttpResult;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.HttpResponseStatus;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class GyAssertUtil {

    public static int checkParam(ChannelHandlerContext ctx, GyHttpRequest request, Long fileSize) {
        //获取默认存储路径
        String downloadPath = request.getDownloadPath();
        if (downloadPath == null) {
            request.setDownloadPath(Main.config.getDownloadPath());
        }
        if (fileSize == 0L) {
            ChannelUtil.send(ctx, HttpResult.notOkJson(400, "downloadUrl error"), HttpResponseStatus.OK);
            return -1;
        }
        if (GyCache.threadMap.get(request.getDownloadUrl()) != null) {
            ChannelUtil.send(ctx, HttpResult.notOkJson(400, "download job is already exist"), HttpResponseStatus.OK);
            return -1;
        }
        return 0;
    }

    public static int checkParam(ChannelHandlerContext ctx, GyHttpRequest request) {
        //获取默认存储路径
        String downloadPath = request.getDownloadPath();
        if (downloadPath == null) {
            request.setDownloadPath(Main.config.getDownloadPath());
        }
        if (request.getDownloadUrl() == null) {
            ChannelUtil.send(ctx, HttpResult.notOkJson(400, "downloadUrl error"), HttpResponseStatus.OK);
            return -1;
        }
        return 0;
    }

}
