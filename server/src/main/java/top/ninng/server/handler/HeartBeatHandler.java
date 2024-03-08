package top.ninng.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 心跳处理器
 *
 * @Author OhmLaw
 * @Date 2024/1/30 15:39
 * @Version 1.0
 */
public class HeartBeatHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(HeartBeatHandler.class);

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        ctx.channel().close();
        cause.printStackTrace();
        logger.error("客户端连接异常，已断开\n" + cause.toString());
    }

    /**
     * 服务器读超时
     *
     * @param ctx
     * @param evt
     */
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) {
        ctx.channel().close();
        logger.error("服务端读超时，已断开连接");
    }
}
