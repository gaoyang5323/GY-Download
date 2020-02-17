package com.kakuiwong.handler;

import com.kakuiwong.annotation.GyHttp;
import com.kakuiwong.contant.HttpPathContant;
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
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class HttpServerHandler extends SimpleChannelInboundHandler<GyHttpRequest> {

    private static Logger log = LoggerFactory.getLogger(HttpServerHandler.class);
    private static Map<String, Method> httpMethod = new HashMap<>();
    private static GyHttpController httpController = new GyHttpController();
    private static Map<String, String> htmlPathMap = new ConcurrentHashMap<>();

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
            String html = null;
            try {
                if (uri.matches("^.*\\.js$")) {
                    resultHtml(channelHandlerContext, getHtmlPath(uri, HtmlType.JS), HtmlType.JS.getName());
                    return;
                }
                if (uri.matches("^.*\\.css$")) {
                    resultHtml(channelHandlerContext, getHtmlPath(uri, HtmlType.CSS), HtmlType.CSS.getName());
                    return;
                }
                resultHtml(channelHandlerContext, getHtmlPath(uri, HtmlType.HTML), HtmlType.HTML.getName());
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

    private void resultHtml(ChannelHandlerContext ctx, String html, String type) {
        if (html == null) {
            html = getHtmlPath("404", HtmlType.HTML);
        }
        HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
        response.headers().set(HttpHeaderNames.CONTENT_TYPE, type);
        File file = new File(html);
        response.headers().set(HttpHeaderNames.CONTENT_LENGTH.toString(), file.length());
        ctx.write(response);
        ctx.write(new DefaultFileRegion(file, 0, file.length()));
        ctx.writeAndFlush(LastHttpContent.EMPTY_LAST_CONTENT).addListener(ChannelFutureListener.CLOSE);
    }

    private void result(ChannelHandlerContext channelHandlerContext, GyHttpRequest gyHttpRequest
            , GyHttpController httpController, Map<String, Method> httpMethod) throws InvocationTargetException, IllegalAccessException {
        Method method = httpMethod.get(gyHttpRequest.getUri());
        if (method == null) {
            ChannelUtil.send(channelHandlerContext, HttpResult.notOkJson(400, "uri error"), HttpResponseStatus.OK, gyHttpRequest.getKeepAlive());
            return;
        }
        method.invoke(httpController, channelHandlerContext, gyHttpRequest);
    }

    private enum HtmlType {
        CSS("text/css; charset=UTF-8"),
        JS("text/javascript; charset=UTF-8"),
        HTML("text/html; charset=UTF-8"),
        NOTFOUNT("text/html; charset=UTF-8");
        private String name;

        HtmlType(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    public String getHtmlPath(String uri, HtmlType htmlType) {
        return htmlPathMap.computeIfAbsent(uri, k -> {
            String defaultDir = System.getProperty("user.dir") + File.separator + HttpPathContant.CONFIG_PATH;
            String localPath = null;
            String realPath = null;
            String realDir = null;
            switch (htmlType) {
                case JS:
                case CSS:
                    localPath = uri.substring(1);
                    realDir = defaultDir + uri.substring(0, uri.lastIndexOf("/"));
                    break;
                case HTML:
                    localPath = "html" + uri + ".html";
                    realDir = defaultDir + File.separator + "html";
                    break;
                default:
                    localPath = "html" + "404.html";
                    realDir = defaultDir + File.separator + "html";
                    break;
            }
            realPath = defaultDir + File.separator + localPath;
            File realPathFile = new File(realPath);
            File realDirFile = new File(realDir);

            if (!realPathFile.exists()) {
                realDirFile.mkdirs();
                try (InputStream inputStream = HttpServerHandler.class.getClassLoader().getResourceAsStream(localPath)) {
                    if (inputStream == null) {
                        return null;
                    }
                    realPathFile.createNewFile();
                    byte[] b = new byte[inputStream.available()];
                    inputStream.read(b);
                    Files.write(Paths.get(realPath), b);
                } catch (IOException e) {
                }
            }
            return realPath;
        });
    }
}
