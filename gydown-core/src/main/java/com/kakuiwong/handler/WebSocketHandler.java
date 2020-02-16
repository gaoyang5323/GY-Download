package com.kakuiwong.handler;

import com.alibaba.fastjson.JSON;
import com.kakuiwong.cache.GyCache;
import com.kakuiwong.domain.AllJobsProperties;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

import java.util.Collection;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class WebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame o) throws Exception {
        String text = o.text();
        if (text != null && text.equals("first")) {
            Collection<AllJobsProperties> values = GyCache.allJobsMap.values();
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame()
                    .replace(Unpooled.copiedBuffer(JSON.toJSONString(values)
                            .getBytes("utf-8"))));
            return;
        }
        if (text != null && text.equals("ping")) {
            channelHandlerContext.writeAndFlush(new TextWebSocketFrame("pong"));
            return;
        }
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        if (GyCache.channelList.size() > 2) {
            ChannelFuture future = ctx.writeAndFlush(new TextWebSocketFrame("超过连接数"));
            future.addListener(ChannelFutureListener.CLOSE);
            return;
        }
        GyCache.channelList.add(ctx);
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        GyCache.channelList.remove(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        GyCache.channelList.remove(ctx);
        ctx.close();
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            String type = "";
            if (event.state() == IdleState.READER_IDLE) {
                type = "read idle";
            } else if (event.state() == IdleState.WRITER_IDLE) {
                type = "write idle";
            } else if (event.state() == IdleState.ALL_IDLE) {
                type = "all idle";
            }
            GyCache.channelList.remove(ctx);
            ctx.writeAndFlush("time out").addListener(
                    ChannelFutureListener.CLOSE);
            System.out.println(ctx.channel().remoteAddress() + "超时类型：" + type);
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
