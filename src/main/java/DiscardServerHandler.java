import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

/**
 * Created by zhenghuasheng on 2016/11/17.
 */
public class DiscardServerHandler extends ChannelInboundHandlerAdapter {


    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ByteBuf in = (ByteBuf) msg;
        try {
            while (in.isReadable()) { // (1)
                System.out.print((char) in.readByte());
                System.out.flush();
            }
        } finally {
            ReferenceCountUtil.release(msg); // (2)
        }
    }

    //ChannelHandlerContext通道处理上下文
    @Override
    public void channelActive(final ChannelHandlerContext ctx) throws InterruptedException { // (1)

        while (true) {
            ByteBuf msgcontent = ctx.alloc().buffer(64); //为ByteBuf分配四个字节
            msgcontent.writeBytes("massage".getBytes());
            ctx.writeAndFlush(msgcontent); // (3)
            Thread.sleep(2000);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) { // (4)
        // Close the connection when an exception is raised.
        cause.printStackTrace();
        ctx.close();
    }
}
