package com.kakuiwong.handler;

import com.kakuiwong.Main;
import com.kakuiwong.util.ChannelUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class WebsocketLoginHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(WebsocketLoginHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            if (null != msg && msg instanceof FullHttpRequest) {
                FullHttpRequest request = (FullHttpRequest) msg;
                String uri = request.uri();
                if (null != uri && ((uri.contains("/gyws") && uri.contains("?")) || uri.contains("/gyws"))) {
                    String[] uriArray = uri.split("\\?");
                    if (null == uriArray || uriArray.length < 2) {
                        ChannelUtil.sendError(ctx, "password error");
                        return;
                    }
                    String[] paramsArray = uriArray[1].split("=");
                    if (null != paramsArray && paramsArray.length > 1) {
                        if (!"token".equals(paramsArray[0]) || !paramsArray[1].equals(Main.config.getPassword())) {
                            ChannelUtil.sendError(ctx, "password error");
                            return;
                        }
                    }
                    request.setUri("/gyws");
                }
            }
            ctx.fireChannelRead(msg);
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
