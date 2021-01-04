package com.netty.server;

import com.netty.message.AskMsg;
import com.netty.message.BaseMsg;
import com.netty.message.LoginMsg;
import com.netty.message.MsgType;
import com.netty.message.PingMsg;
import com.netty.message.ReplyClientBody;
import com.netty.message.ReplyMsg;
import com.netty.message.ReplyServerBody;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by zhenghuasheng on 2016/11/18.
 */
public class NettyServerHandler extends SimpleChannelInboundHandler<BaseMsg> {

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //channel失效，从Map中移除
        NettyChannelMap.remove((SocketChannel)ctx.channel());
    }


    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleState state = ((IdleStateEvent) evt).state();
            if (state == IdleState.READER_IDLE) {
                // 在规定时间内没有收到客户端的上行数据, 主动断开连接
                System.out.println("未接到数据,断开了client连接");
                ctx.disconnect();
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }


    @Override
    protected void messageReceived(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {

        if(MsgType.LOGIN.equals(baseMsg.getType())){
            LoginMsg loginMsg=(LoginMsg)baseMsg;
            if("robin".equals(loginMsg.getUserName())&&"yao".equals(loginMsg.getPassword())){
                //登录成功,把channel存到服务端的map中
                NettyChannelMap.add(loginMsg.getClientId(),(SocketChannel)channelHandlerContext.channel());
                System.out.println("client"+loginMsg.getClientId()+" 登录成功");
            }
        }else{
            if(NettyChannelMap.get(baseMsg.getClientId())==null){
                //说明未登录，或者连接断了，服务器向客户端发起登录请求，让客户端重新登录
                LoginMsg loginMsg=new LoginMsg();
                channelHandlerContext.channel().writeAndFlush(loginMsg);
            }
        }
        switch (baseMsg.getType()){
            case PING:{
                PingMsg pingMsg=(PingMsg)baseMsg;
                PingMsg replyPing=new PingMsg();
                Channel channel = NettyChannelMap.get(pingMsg.getClientId());
                if (channel == null) {
                    return;
                }
                channel.writeAndFlush(replyPing);
            }break;
            case ASK:{
                //收到客户端的请求
                AskMsg askMsg=(AskMsg)baseMsg;
                if("authToken".equals(askMsg.getParams().getAuth())){
                    ReplyServerBody replyBody=new ReplyServerBody("server info $$$$ !!!");
                    ReplyMsg replyMsg=new ReplyMsg();
                    replyMsg.setBody(replyBody);
                    NettyChannelMap.get(askMsg.getClientId()).writeAndFlush(replyMsg);
                }
            }break;
            case REPLY:{
                //收到客户端回复
                ReplyMsg replyMsg=(ReplyMsg)baseMsg;
                ReplyClientBody clientBody=(ReplyClientBody)replyMsg.getBody();
                System.out.println("receive client msg: "+clientBody.getClientInfo());
            }break;
            default:break;
        }
        ReferenceCountUtil.release(baseMsg);
    }

}
