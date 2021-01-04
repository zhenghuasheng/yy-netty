package com.netty.client;

import com.netty.client.thread.RetryTask;
import com.netty.message.BaseMsg;
import io.netty.channel.*;

import java.io.IOException;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author zhs
 * @Description
 * @createTime 2020/12/29 0029 15:09
 */
public class ReconnectHandler extends SimpleChannelInboundHandler<BaseMsg> {

    private static int retries = 0;
    private RetryPolicy retryPolicy;

    private NettyClientBootstrap tcpClient;

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(5, 10, 1L, TimeUnit.SECONDS,
            new ArrayBlockingQueue<Runnable>(100),
            new ThreadPoolExecutor.CallerRunsPolicy());




    public ReconnectHandler(NettyClientBootstrap tcpClient) {
        this.tcpClient = tcpClient;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.out.println("Successfully established a connection to the server.");
        retries = 0;
        ctx.fireChannelActive();
    }


    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        if (retries == 0) {
            System.err.println("Lost the TCP connection with the server.");
            ctx.close();
        }

        boolean allowRetry = getRetryPolicy().allowRetry(retries);
        if (allowRetry) {

            long sleepTimeMs = getRetryPolicy().getSleepTimeMs(retries);

            System.out.println(String.format("Try to reconnect to the server after %dms. Retry count: %d.", sleepTimeMs, ++retries));

            final EventLoop eventLoop = ctx.channel().eventLoop();
            eventLoop.schedule(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Reconnecting ...");
                    try {
                        tcpClient.connect();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    //executor.submit(new RetryTask(tcpClient));
                }
            }, sleepTimeMs, TimeUnit.MILLISECONDS);

        }
        ctx.fireChannelInactive();
    }


    private RetryPolicy getRetryPolicy() {
        if (this.retryPolicy == null) {
            this.retryPolicy = tcpClient.getRetryPolicy();
        }
        return this.retryPolicy;
    }


    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {

    }
}
