package com.netty.client.thread;

import com.netty.client.NettyClientBootstrap;

import java.io.IOException;

/**
 * @author zhs
 * @Description
 * @createTime 2020/12/29 0029 17:54
 */
public class RetryTask implements Runnable{

    private NettyClientBootstrap  tcpClient;

    public RetryTask(NettyClientBootstrap tcpClient) {
        this.tcpClient = tcpClient;
    }


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

    }
}
