package com.netty.client;

import com.netty.message.AskMsg;
import com.netty.message.AskParams;
import com.netty.message.Constants;
import com.netty.message.LoginMsg;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.util.concurrent.TimeUnit;

/**
 * Created by zhenghuasheng on 2016/11/18.
 */
public class NettyClientBootstrap {
    private int port;
    private String host;

    public SocketChannel getSocketChannel() {
        return socketChannel;
    }

    private SocketChannel socketChannel;

    private Bootstrap bootstrap;

    /** 重连策略 */
    private RetryPolicy retryPolicy = new ExponentialBackOffRetry(1000, Integer.MAX_VALUE, 60 * 1000);


    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(20);

    public NettyClientBootstrap(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;
        start();
    }
    private void start() throws InterruptedException {
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        bootstrap=new Bootstrap();
        bootstrap.channel(NioSocketChannel.class);
        bootstrap.option(ChannelOption.SO_KEEPALIVE,true);
        bootstrap.group(eventLoopGroup);
        bootstrap.remoteAddress(host,port);
        bootstrap.handler(new ChannelInitializer<SocketChannel>() {
            @Override
            protected void initChannel(SocketChannel socketChannel) throws Exception {
                socketChannel.pipeline().addLast(new IdleStateHandler(20,2,0));
                socketChannel.pipeline().addLast(new ObjectEncoder());
                socketChannel.pipeline().addLast(new ObjectDecoder(ClassResolvers.cacheDisabled(null)));
                socketChannel.pipeline().addLast(new NettyClientHandler());
                socketChannel.pipeline().addLast(new ReconnectHandler(NettyClientBootstrap.this));
                socketChannel.pipeline().addLast(group);
            }
        });
//        ChannelFuture future =bootstrap.connect(host,port).sync();
//        if (future.isSuccess()) {
//            socketChannel = (SocketChannel)future.channel();
//            System.out.println("connect server  成功---------");
//        }
    }
    private ChannelFutureListener getConnectionListener() {
        return new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture future) throws Exception {
                if (!future.isSuccess()) {
                    future.channel().pipeline().fireChannelInactive();
                }
            }
        };
    }
    /**
     * 向远程TCP服务器请求连接
     */
    public boolean connect() throws InterruptedException {
        synchronized (bootstrap) {
            try {
                ChannelFuture future =bootstrap.connect(host,port).sync();
                if (future.isSuccess()) {
                    future.addListener(getConnectionListener());
                    socketChannel = (SocketChannel)future.channel();
                    System.out.println("connect server  成功---------");
                    return true;
                }
            } catch (InterruptedException e) {
               e.printStackTrace();
            }

        }
        return false;
    }

    public RetryPolicy getRetryPolicy() {
        return retryPolicy;
    }


    public static void main(String[]args) throws InterruptedException {
        Constants.setClientId("001");
        NettyClientBootstrap nbc = new NettyClientBootstrap(9999,"localhost");
        nbc.connect();


        LoginMsg loginMsg=new LoginMsg();
        loginMsg.setPassword("yao");
        loginMsg.setUserName("robin");
        nbc.socketChannel.writeAndFlush(loginMsg);
        while (true){
            TimeUnit.SECONDS.sleep(3);
            AskMsg askMsg=new AskMsg();
            AskParams askParams=new AskParams();
            askParams.setAuth("authToken");
            askMsg.setParams(askParams);
            nbc.socketChannel.writeAndFlush(askMsg);
        }
    }
}
