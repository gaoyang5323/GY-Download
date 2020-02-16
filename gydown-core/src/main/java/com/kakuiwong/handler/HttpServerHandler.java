package com.kakuiwong.handler;

import com.kakuiwong.annotation.GyHttp;
import com.kakuiwong.controller.GyHttpController;
import com.kakuiwong.domain.GyHttpRequest;
import com.kakuiwong.domain.HttpResult;
import com.kakuiwong.util.ChannelUtil;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.DefaultFileRegion;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<GyHttpRequest> {

    private static Logger log = LoggerFactory.getLogger(HttpServerHandler.class);
    private static Map<String, Method> httpMethod = new HashMap<>();
    private static GyHttpController httpController = new GyHttpController();

    static {
        Method[] methods = httpController.getClass().getDeclaredMethods();
        Arrays.stream(methods).forEach(method -> {
            GyHttp annotation = method.getAnnotation(GyHttp.class);
            if (annotation == null) {
                return;
            }
            httpMethod.put(annotation.value(), method);
        });
    }

    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, GyHttpRequest gyHttpRequest) throws Exception {
        String uri = gyHttpRequest.getUri();
        if (gyHttpRequest.getMethod().equalsIgnoreCase("POST")) {
            result(channelHandlerContext, gyHttpRequest, httpController, httpMethod);
            return;
        }
        if (gyHttpRequest.getMethod().equalsIgnoreCase("GET")) {
            String html = HttpServerHandler.class.getClassLoader().getResource("html/404.html").getPath();
            try {
                if (uri.matches("^.*\\.js$")) {
                    html = HttpServerHandler.class.getClassLoader().getResource(uri.substring(1)).getPath();
                    resultHtml(channelHandlerContext, html, "javascript");
                    return;
                }
                if (uri.matches("^.*\\.css$")) {
                    html = HttpServerHandler.class.getClassLoader().getResource(uri.substring(1)).getPath();
                    resultHtml(channelHandlerContext, html, "css");
                    return;
                }
                html = HttpServerHandler.class.getClassLoader().getResource("html" + uri + ".html").getPath();
                resultHtml(channelHandlerContext, html, "html");
                return;
            } catch (Exception e) {
                log.error("channelRead0", e);
            }
            return;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }

    private void resultHtml(ChannelHandlerContext ctx, String html, String type) throws InvocationTargetException, IllegalAccessException {
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, "text/" + type + "; charset=UTF-8");
        ctx.write(response);
        File file = new File(html);
        ctx.write(new DefaultFileRegion(file, 0, file.length()));
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
    }

    private void result(ChannelHandlerContext channelHandlerContext, GyHttpRequest gyHttpRequest
            , GyHttpController httpController, Map<String, Method> httpMethod) throws InvocationTargetException, IllegalAccessException {
        Method method = httpMethod.get(gyHttpRequest.getUri());
        if (method == null) {
            ChannelUtil.send(channelHandlerContext, HttpResult.notOkJson(400, "uri error"), HttpResponseStatus.OK);
            return;
        }
        method.invoke(httpController, channelHandlerContext, gyHttpRequest);
    }
}
