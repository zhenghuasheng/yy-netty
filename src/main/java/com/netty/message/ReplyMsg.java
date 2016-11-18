package com.netty.message;

/**
 * Created by zhenghuasheng on 2016/11/18.
 */
public class ReplyMsg extends BaseMsg{
    public ReplyMsg() {
        super();
        setType(MsgType.REPLY);
    }
    private ReplyBody body;

    public ReplyBody getBody() {
        return body;
    }

    public void setBody(ReplyBody body) {
        this.body = body;
    }
}
