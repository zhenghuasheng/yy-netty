package com.netty.message;

/**
 * 心跳检测Ping类型消息
 * Created by zhenghuasheng on 2016/11/18.
 */
public class PingMsg extends BaseMsg {
    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}
