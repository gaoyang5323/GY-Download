package com.kakuiwong.util;

import com.kakuiwong.domain.HttpResult;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.*;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class ChannelUtil {

    public static void close(ChannelFuture future, Boolean isKeepAlive) {
        if (!(isKeepAlive != null && isKeepAlive)) {
            future.addListener(ChannelFutureListener.CLOSE);
        }
    }

    public static void send(ChannelHandlerContext ctx, String context, HttpResponseStatus status) {
        FullHttpResponse response = new DefaultFullHttpResponse(HttpVersion.HTTP_1_1, status, Unpooled.copiedBuffer(context, CharsetUtil.UTF_8));
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "application/json; charset=UTF-8");
        response.headers().set("Access-Control-Allow-Origin", "*");
        response.headers().set("Access-Control-Allow-Methods", "POST, GET, OPTIONS, DELETE");
        response.headers().set("Access-Control-Max-Age", "3600");
        response.headers().set("Access-Control-Allow-Headers", "*");
        ChannelFuture channelFuture = ctx.writeAndFlush(response);
        channelFuture.addListener(ChannelFutureListener.CLOSE);
    }

    public static void sendError(ChannelHandlerContext ctx, String msg) {
        ChannelUtil.send(ctx, HttpResult.notOkJson(400, msg), HttpResponseStatus.OK);
    }

    public static void release(Object msg) {
        boolean flag = false;
        do {
            flag = ReferenceCountUtil.release(msg);
        } while (!flag);
    }
}
