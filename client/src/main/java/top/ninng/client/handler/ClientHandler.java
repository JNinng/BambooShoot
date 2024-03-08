package top.ninng.client.handler;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.bytes.ByteArrayDecoder;
import io.netty.handler.codec.bytes.ByteArrayEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ninng.client.Config;
import top.ninng.client.Connect;
import top.ninng.client.helper.BootStrapHelper;
import top.ninng.common.constant.C;
import top.ninng.common.protocol.ProxyMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 服务器连接处理器
 *
 * @Author OhmLaw
 * @Date 2024/1/28 14:45
 * @Version 1.0
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(ClientHandler.class);
    /**
     * 服务器端口和客户端端口映射
     * key: 服务器公开端口
     * value: 客户端公开端口
     */
    private Map<Integer, Integer> portMap = new ConcurrentHashMap<>();

    /**
     * 连接建立，初始化
     *
     * @param ctx
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        // 保存与服务端的隧道
        Connect.INSTANCE.setTunnel(ctx);
        // 连接建立成功，开始注册
        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(ProxyMessage.TYPE_REGISTER);

        // 元数据
        Map<String, Object> metaData = new HashMap<>();
        // 存入 Key
        metaData.put(C.AUTH_KEY, Config.get(C.AUTH_KEY));
        // 获取端口映射
        for (Map<String, Object> portMapping : Config.getPortMappingList()) {
            portMap.put(
                    (Integer) portMapping.get(C.SERVER_PORT),
                    (Integer) portMapping.get(C.CLIENT_PORT));
        }
        ArrayList<Integer> serverPublicPortList = new ArrayList<>(portMap.keySet());
        // 存入请求公开的端口
        metaData.put(C.PUBLIC_PORTS, serverPublicPortList);
        proxyMessage.setMetaData(metaData);

        logger.info("[client] 注册：" + proxyMessage + " >> [server]");
        // 突突突突
        ctx.writeAndFlush(proxyMessage);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) {
        Connect.INSTANCE.close();
    }

    /**
     * 接收消息
     *
     * @param ctx
     * @param msg
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        ProxyMessage proxyMessage = (ProxyMessage) msg;
        switch (proxyMessage.getType()) {
            // 授权
            case ProxyMessage.TYPE_AUTH:
                auth(proxyMessage);
                break;
            // 外部访问服务器，建立连接
            case ProxyMessage.TYPE_CONNECTED:
                connected(proxyMessage);
                break;
            // 断开连接
            case ProxyMessage.TYPE_DISCONNECTED:
                disconnected(proxyMessage);
                break;
            // 数据传输
            case ProxyMessage.TYPE_DATA:
                data(proxyMessage, ctx);
                break;
            // 心跳
            case ProxyMessage.TYPE_KEEPALIVE:
                ;
            default:
                break;
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("连接异常中断：" + cause.toString());
        cause.printStackTrace();
        // 传递异常
        ctx.fireExceptionCaught(cause);
        Connect.INSTANCE.close();
    }

    private void auth(ProxyMessage proxyMessage) {
        if ((Boolean) proxyMessage.getMetaData().get(C.IS_AUTH)) {
            logger.info("注册成功：" + proxyMessage.getMetaData().get(C.AUTH_RESULT));
        } else {
            logger.info("注册失败：" + portMap + "\n原因：" + proxyMessage.getMetaData().get(C.AUTH_RESULT));
            Connect.INSTANCE.close();
        }
    }

    /**
     * 外部访问服务端注册端口，建立内部连接通道
     *
     * @param proxyMessage
     */
    private void connected(ProxyMessage proxyMessage) {
        String publicChannelId = (String) proxyMessage.getMetaData().get(C.CHANNEL_ID);
        BootStrapHelper bootStrapHelper = new BootStrapHelper();
        ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {

            @Override
            protected void initChannel(Channel ch) {
                LocalHandler localHandler = new LocalHandler(publicChannelId);
                ch.pipeline().addLast(
                        new ByteArrayEncoder(),
                        new ByteArrayDecoder(),
                        localHandler);
                Connect.INSTANCE.addChannel(ch);
            }
        };
        String localhost = (String) Config.get(C.LOCALHOST);
        int serverPublicPort = (int) proxyMessage.getMetaData().get(C.PUBLIC_PORT);
        int localPort = portMap.get(serverPublicPort);
        bootStrapHelper.start(Connect.INSTANCE.getLocalGroup(), channelInitializer, localhost, localPort);
        logger.info("S[" + serverPublicPort + "] <==> local[" + localPort + "] 注册代理端口请求处理器");
    }

    /**
     * 服务器请求数据处理
     *
     * @param proxyMessage
     * @param ctx
     */
    private void data(ProxyMessage proxyMessage, ChannelHandlerContext ctx) {
        if (proxyMessage.getData() == null || proxyMessage.getData().length <= 0) {
            return;
        }
        String publicChannelId = (String) proxyMessage.getMetaData().get(C.CHANNEL_ID);
        logger.info("S[" + ctx.channel().remoteAddress() + "] >>>> C[" + Config.get(C.PORT) + "] (DATA " + proxyMessage.getData().length + " byte)");
        // 发送到对应处理程序
        Connect.INSTANCE.publicChannelWriteAndFlush(publicChannelId, proxyMessage.getData());
    }

    /**
     * 外部与服务器连接断开
     *
     * @param proxyMessage
     */
    private void disconnected(ProxyMessage proxyMessage) {
        String publicChannelId = (String) proxyMessage.getMetaData().get(C.CHANNEL_ID);
        Connect.INSTANCE.publicChannelClose(publicChannelId);
    }
}
