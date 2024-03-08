package top.ninng.server.handler;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ninng.common.constant.C;
import top.ninng.common.protocol.ProxyMessage;
import top.ninng.server.Connect;

import java.util.HashMap;
import java.util.Map;

/**
 * @Author OhmLaw
 * @Date 2024/1/30 16:35
 * @Version 1.0
 */
public class PublicHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(PublicHandler.class);
    private String channelId;
    private String address;
    private String authKey;
    private int publicPort;

    public PublicHandler(String authKey, int publicPort) {
        this.authKey = authKey;
        this.publicPort = publicPort;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        Connect.INSTANCE.addPublicChannel(ctx.channel());
        channelId = ctx.channel().id().asLongText();
        address = String.valueOf(ctx.channel().remoteAddress());
        logger.info("P[" + address + "] <==> server[" + publicPort + "] channelId: " + channelId);
        forwardToClient(ProxyMessage.TYPE_CONNECTED, channelId, null);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.info("P[" + address + "] <=/=> server[" + publicPort + "] channelId: " + channelId);
        forwardToClient(ProxyMessage.TYPE_DISCONNECTED, ctx.channel().id().asLongText(), null);
        Connect.INSTANCE.removePublicChannel(channelId);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        byte[] data = (byte[]) msg;
        logger.info("P[" + ctx.channel().remoteAddress() + "] >>>> server[" + publicPort + "] (DATA " + data.length + " byte)");
        forwardToClient(ProxyMessage.TYPE_DATA, ctx.channel().id().asLongText(), data);
    }

    /**
     * 将请求转发到客户端
     */
    public void forwardToClient(short type, String channelId, byte[] bytes) {
        ChannelHandlerContext client = Connect.INSTANCE.getClient(authKey);
        if (client == null) {
            logger.error("与客户端 " + authKey + " 的通道不存在，转发失败");
            return;
        }
        ProxyMessage proxyMessage = new ProxyMessage(type);
        Map<String, Object> metaData = new HashMap<>();
        metaData.put(C.CHANNEL_ID, channelId);
        metaData.put(C.PUBLIC_PORT, publicPort);
        proxyMessage.setMetaData(metaData);
        int length = 0;
        if (bytes != null) {
            proxyMessage.setData(bytes);
            length = bytes.length;
        }

        client.writeAndFlush(proxyMessage);
        logger.info("S[" + publicPort + "] >>>> client[" + address + "] [" +
                ProxyMessage.typeToString(type) + "] (DATA " + length + " byte)");
    }
}
