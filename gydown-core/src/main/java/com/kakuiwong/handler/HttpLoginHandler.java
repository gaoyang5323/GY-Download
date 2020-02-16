package com.kakuiwong.handler;

import com.kakuiwong.Main;
import com.kakuiwong.contant.HttpLoginIgnore;
import com.kakuiwong.util.ChannelUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.codec.http.FullHttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class HttpLoginHandler extends ChannelInboundHandlerAdapter {

    private static Logger log = LoggerFactory.getLogger(HttpLoginHandler.class);

    @Override
    public void channelRead(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        try {
            if (msg instanceof FullHttpRequest) {
                FullHttpRequest fullHttpRequest = (FullHttpRequest) msg;
                String uri = fullHttpRequest.uri();
                if ("/favicon.ico".equals(uri)) {
                    return;
                }
                boolean ignoreUri = Arrays.stream(HttpLoginIgnore.HTML_PATH.split(",")).anyMatch(path -> path.equals(uri));
                boolean ignoreResourse = uri.matches("^.*\\.js$") || uri.matches("^.*\\.css$");
                if (!ignoreUri && !ignoreResourse) {
                    String token = fullHttpRequest.headers().getAsString("token");
                    if (token == null || !token.equals(Main.config.getPassword())) {
                        ChannelUtil.sendError(channelHandlerContext, "token error");
                        return;
                    }
                }
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
