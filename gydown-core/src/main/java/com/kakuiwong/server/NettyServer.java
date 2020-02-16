package com.kakuiwong.server;

import com.kakuiwong.domain.CoreYmlConfig;
import com.kakuiwong.handler.*;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.HttpServerExpectContinueHandler;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.ResourceLeakDetector;

/**
 * @author gaoyang
 * @email 785175323@qq.com
 */
public class NettyServer {
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;
    private Channel serverChannel;
    private final static NettyServer nettyServer = new NettyServer();

    public static NettyServer getInstance() {
        return nettyServer;
    }

    public void stopServerChannel() {
        serverChannel.close();
    }

    public void start(CoreYmlConfig config) throws InterruptedException {
        if (bossGroup == null) {
            bossGroup = new NioEventLoopGroup(2);
        }
        if (workerGroup == null) {
            workerGroup = new NioEventLoopGroup(4);
        }
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .option(ChannelOption.SO_BACKLOG, 1024)
                .handler(new LoggingHandler(LogLevel.DEBUG))
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel sc) throws Exception {
                        ChannelPipeline pipeline = sc.pipeline();
                        addHandler(pipeline);
                    }
                });
        ResourceLeakDetector.setLevel(ResourceLeakDetector.Level.ADVANCED);
        ChannelFuture cf = b.bind(config.getPort()).sync();
        serverChannel = cf.channel();
        serverChannel.closeFuture().sync();
    }

    private void addHandler(ChannelPipeline pipeline) {
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new ChunkedWriteHandler());
        pipeline.addLast(new HttpObjectAggregator(1024 * 1024));
        pipeline.addLast(new HttpServerExpectContinueHandler());
        pipeline.addLast(new IdleStateHandler(60, 60 * 60, 60 * 60));

        pipeline.addLast(new WebsocketLoginHandler());
        pipeline.addLast(new WebSocketServerProtocolHandler("/gyws", null, true, 65536 * 10));
        pipeline.addLast(new WebSocketHandler());

        pipeline.addLast(new HttpLoginHandler());
        pipeline.addLast(new HttpJsonConventHandler());
        pipeline.addLast(new HttpServerHandler());
        pipeline.addLast(new TailHandler());
    }

    public void stop() {
        if (bossGroup != null) {
            bossGroup.shutdownGracefully();
        }
        if (workerGroup != null) {
            workerGroup.shutdownGracefully();
        }
    }
}
