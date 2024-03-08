package top.ninng.client.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ninng.client.Config;
import top.ninng.client.Connect;
import top.ninng.common.constant.C;
import top.ninng.common.protocol.ProxyMessage;

import java.util.HashMap;
import java.util.Map;

/**
 * 内部服务处理器
 *
 * @Author OhmLaw
 * @Date 2024/1/28 15:21
 * @Version 1.0
 */
public class LocalHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(LocalHandler.class);
    private final String publicChannelId;

    public LocalHandler(String publicChannelId) {
        this.publicChannelId = publicChannelId;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Connect.INSTANCE.putPublicChannel(publicChannelId, ctx.channel());
        String remoteAddress = ctx.channel().remoteAddress().toString();
        logger.info("client <==> " + "local[" + remoteAddress.substring(remoteAddress.indexOf(":") + 1) +
                "] 建立通道 channelId: " + ctx.channel().id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TYPE_DISCONNECTED);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(C.CHANNEL_ID, publicChannelId);
        proxyMessage.setMetaData(metaData);

        // 内部服务断开，返回给代理服务器
        Connect.INSTANCE.tunnelWriteAndFlush(proxyMessage);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte[] bytes = (byte[]) msg;
        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TYPE_DATA);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(C.CHANNEL_ID, publicChannelId);
        metaData.put(C.AUTH_KEY, Config.get(C.AUTH_KEY));
        proxyMessage.setMetaData(metaData);
        proxyMessage.setData(bytes);

        // 内部服务响应返回给代理服务器
        Connect.INSTANCE.tunnelWriteAndFlush(proxyMessage);
        logger.info("S[" + Connect.INSTANCE.getTunnel().channel().remoteAddress() + "] <<<< C[" + Config.get(C.PORT) +
                "] (DATA " + bytes.length + " " + "byte)");
    }
}
