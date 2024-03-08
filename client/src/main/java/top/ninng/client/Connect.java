package top.ninng.client;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import top.ninng.common.protocol.ProxyMessage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 全局连接
 *
 * @Author OhmLaw
 * @Date 2024/1/29 14:16
 * @Version 1.0
 */
public enum Connect {

    INSTANCE;

    /**
     * 全局管理 Channel
     */
    private ChannelGroup channels;
    /**
     * 所有 Channel 共享，减少线程上下文切换
     */
    private EventLoopGroup localGroup;
    /**
     * key: channelId
     */
    private Map<String, Channel> publicChannelMap;
    /**
     * 与服务端通讯隧道程序
     */
    private ChannelHandlerContext tunnel;
    private EventLoopGroup workerGroup;

    Connect() {
        workerGroup = new NioEventLoopGroup();
        channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
        localGroup = new NioEventLoopGroup();
        publicChannelMap = new ConcurrentHashMap<>();
    }

    public boolean addChannel(Channel channel) {
        return channels.add(channel);
    }

    public void close() {
        tunnelChannelClose();
        channels.close();
        localGroup.shutdownGracefully();
        workerGroupShutdownGracefully();
    }

    public ChannelGroup getChannels() {
        return channels;
    }

    public EventLoopGroup getLocalGroup() {
        return localGroup;
    }

    public Map<String, Channel> getPublicChannelMap() {
        return publicChannelMap;
    }

    public ChannelHandlerContext getTunnel() {
        return tunnel;
    }

    public void setTunnel(ChannelHandlerContext tunnel) {
        this.tunnel = tunnel;
    }

    public EventLoopGroup getWorkerGroup() {
        return workerGroup;
    }

    public void publicChannelClose(String channelId) {
        Channel channel = publicChannelMap.get(channelId);
        if (channel != null) {
            channel.close();
            publicChannelMap.remove(channelId);
        }
    }

    public void publicChannelWriteAndFlush(String channelId, byte[] bytes) {
        Channel channel = publicChannelMap.get(channelId);
        if (channel != null) {
            channel.writeAndFlush(bytes);
        }
    }

    public void putPublicChannel(String publicChannelId, Channel channel) {
        publicChannelMap.put(publicChannelId, channel);
    }

    public void tunnelChannelClose() {
        tunnel.channel().close();
    }

    public void tunnelWriteAndFlush(ProxyMessage proxyMessage) {
        tunnel.writeAndFlush(proxyMessage);
    }

    public void workerGroupShutdownGracefully() {
        workerGroup.shutdownGracefully();
    }
}
