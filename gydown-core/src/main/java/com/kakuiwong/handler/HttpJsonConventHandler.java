package com.kakuiwong.handler;

import com.alibaba.fastjson.JSON;
import com.kakuiwong.domain.GyHttpRequest;
import com.kakuiwong.util.ChannelUtil;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.HttpUtil;
import io.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class HttpJsonConventHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(HttpJsonConventHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
                ByteBuf content = fullHttpRequest.content();
                GyHttpRequest gyHttpRequest = null;
                String contentStr = content.toString(CharsetUtil.UTF_8);
                if (contentStr != null) {
                    gyHttpRequest = JSON.parseObject(contentStr, GyHttpRequest.class);
                    if (gyHttpRequest == null) {
                        gyHttpRequest = new GyHttpRequest();
                    }
                }
                gyHttpRequest.setUri(fullHttpRequest.uri());
                gyHttpRequest.setMethod(fullHttpRequest.method().name());
                gyHttpRequest.setKeepAlive(HttpUtil.isKeepAlive(fullHttpRequest));
                channelHandlerContext.fireChannelRead(gyHttpRequest);
                return;
            }
            channelHandlerContext.fireChannelRead(msg);
        } catch (Exception e) {
            log.error("channelRead", e);
            ChannelUtil.release(msg);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
