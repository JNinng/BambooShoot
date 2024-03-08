package top.ninng.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ninng.client.Connect;
import top.ninng.common.protocol.ProxyMessage;

/**
 * 心跳处理器
 *
 * @Author OhmLaw
 * @Date 2024/1/28 14:45
 * @Version 1.0
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);
    /**
     * 心跳消息
     */
    private static final ProxyMessage HEART_BEAT = new ProxyMessage(ProxyMessage.TYPE_KEEPALIVE);

    public HeartBeatHandler() {
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("连接异常中断：" + cause.toString());
        cause.printStackTrace();
        ctx.writeAndFlush(HEART_BEAT);
        ctx.channel().close();
        Connect.INSTANCE.close();
    }

    /**
     * 客户端写超时
     *
     * @param ctx
     * @param evt
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        ctx.writeAndFlush(HEART_BEAT);
    }
}
