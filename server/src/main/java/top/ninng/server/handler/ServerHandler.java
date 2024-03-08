package top.ninng.server.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ninng.common.constant.C;
import top.ninng.common.protocol.ProxyMessage;
import top.ninng.server.Config;
import top.ninng.server.Connect;
import top.ninng.server.helper.BootstrapHelper;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * 客户端通讯处理器
 *
 * @Author OhmLaw
 * @Date 2024/1/30 15:39
 * @Version 1.0
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {

    private final Logger logger = LoggerFactory.getLogger(ServerHandler.class);

    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        logger.info(">> server <==> client[" + ctx.channel().remoteAddress() + "] 已接入建立连接 channelId: " + ctx.channel().id().asLongText());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        logger.error("异常关闭：channelId: " + ctx.channel().id().asLongText());
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ProxyMessage proxyMessage = (ProxyMessage) msg;
        switch (proxyMessage.getType()) {
            case ProxyMessage.TYPE_REGISTER:
                register(proxyMessage, ctx);
                break;
            case ProxyMessage.TYPE_DISCONNECTED:
                disconnected(proxyMessage);
                break;
            case ProxyMessage.TYPE_DATA:
                data(proxyMessage, ctx);
                break;
            case ProxyMessage.TYPE_KEEPALIVE:
                keepAlive(proxyMessage, ctx);
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        Connect.INSTANCE.closeClient(ctx.channel().remoteAddress().toString());
    }

    /**
     * 处理由客户端响应的内部数据
     *
     * @param proxyMessage
     * @param ctx
     */
    private void data(ProxyMessage proxyMessage, ChannelHandlerContext ctx) {
        String authKey = (String) proxyMessage.getMetaData().get(C.AUTH_KEY);
        String channelId = (String) proxyMessage.getMetaData().get(C.CHANNEL_ID);
        if (proxyMessage.getData() == null || proxyMessage.getData().length <= 0) {
            return;
        }
        Channel publicChannel = Connect.INSTANCE.getPublicChannel(channelId);
        publicChannel.writeAndFlush(proxyMessage.getData());
        String publicPort = publicChannel.localAddress().toString();
        logger.info("S[" + Config.get(C.PORT) + "] <<<< client[" + ctx.channel().remoteAddress() + "] (DATA " +
                proxyMessage.getData().length + " byte)");
        logger.info("P[" + publicChannel.remoteAddress() + "] << server[" +
                publicPort.substring(publicPort.indexOf(":") + 1) +
                " < " + Config.get(C.PORT) + "] (DATA " + proxyMessage.getData().length + " byte)");
//        logger.info(publicChannel.remoteAddress() + " << server[" + publicPort.substring(publicPort.indexOf(":") + 1) +
//                " < " + Config.get(C.PORT) +
//                "] << client[" + ctx.channel().remoteAddress() + "] " +
//                "[DATA]data: " + proxyMessage.getData().length + " byte");
    }

    private void disconnected(ProxyMessage proxyMessage) {
        String authKey = (String) proxyMessage.getMetaData().get(C.AUTH_KEY);
        String channelId = (String) proxyMessage.getMetaData().get(C.CHANNEL_ID);

        Connect.INSTANCE.publicChannelClose(channelId);
        logger.info("server <=/=> client[" + authKey + "]已断开：" + channelId);
    }

    private void keepAlive(ProxyMessage proxyMessage, ChannelHandlerContext ctx) {
        logger.info("C[" + ctx.channel().remoteAddress() + "] KEEPALIVE");
    }

    /**
     * 客户端注册消息
     *
     * @param proxyMessage
     * @param ctx
     */
    private void register(ProxyMessage proxyMessage, ChannelHandlerContext ctx) {
        String authKey = (String) proxyMessage.getMetaData().get(C.AUTH_KEY);
        String host = (String) Config.get(C.LOCALHOST);
        ArrayList<Integer> publicPortList = (ArrayList<Integer>) proxyMessage.getMetaData().get(C.PUBLIC_PORTS);
        Connect.INSTANCE.putClient(authKey, ctx.channel().remoteAddress().toString(), ctx, publicPortList);

        BootstrapHelper bootstrapHelper = new BootstrapHelper();
        for (Integer publicPort : publicPortList) {
            ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
                @Override
                protected void initChannel(Channel ch) {
                    PublicHandler publicHandler = new PublicHandler(authKey, publicPort);
                    ch.pipeline().addLast(
                            new ByteArrayDecoder(),
                            new ByteArrayEncoder(),
                            publicHandler);
                    Connect.INSTANCE.addChannels(ctx.channel().remoteAddress().toString(), ch);
                }
            };
            bootstrapHelper.start(
                    Connect.INSTANCE.getClientBossGroup(),
                    Connect.INSTANCE.getClientWorkerGroup(),
                    host,
                    publicPort,
                    channelInitializer);
        }

        Map<String, Object> metaData = new HashMap<>();
        metaData.put(C.IS_AUTH, true);
        metaData.put(C.AUTH_RESULT, publicPortList);
        ProxyMessage resultMessage = new ProxyMessage(ProxyMessage.TYPE_AUTH);
        resultMessage.setMetaData(metaData);
        // 响应注册结果
        ctx.writeAndFlush(resultMessage);
    }
}
