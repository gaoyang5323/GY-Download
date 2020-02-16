package com.kakuiwong.controller;

import com.kakuiwong.domain.GyHttpRequest;
import io.netty.channel.ChannelHandlerContext;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public interface GyController {

    void startDownload(ChannelHandlerContext ctx, GyHttpRequest request);

    void downloadStop(ChannelHandlerContext ctx, GyHttpRequest request);

    void downloadDelete(ChannelHandlerContext ctx, GyHttpRequest request);

    void downloadShutdown(ChannelHandlerContext ctx, GyHttpRequest request);
}
