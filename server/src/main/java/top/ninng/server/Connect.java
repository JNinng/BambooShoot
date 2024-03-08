package top.ninng.server;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.util.concurrent.GlobalEventExecutor;

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @Author OhmLaw
 * @Date 2024/1/30 16:17
 * @Version 1.0
 */
public enum Connect {

    INSTANCE;

    /**
     * 全局管理 Channel
     * key: ctx.channel().remoteAddress().toString()
     * value: {@link ChannelGroup}
     */
    private Map<String, ChannelGroup> channelGroupMap;
    /**
     * key: channelId
     */
    private Map<String, Channel> publicChannelMap;
    /**
     * 客户端组
     */
    private EventLoopGroup clientBossGroup, clientWorkerGroup;
    /**
     * 公开端口组
     */
    private EventLoopGroup publicBossGroup, publicWorkerGroup;
    private Map<String, Channel> publicBindChannel;
    /**
     * 服务器与客户端的通讯隧道
     * key: ctx.channel().remoteAddress().toString()
     * value: {@link ChannelHandlerContext}
     */
    private Map<String, ChannelHandlerContext> clientMap;
    /**
     * 客户端申请的公开端口
     * key: ctx.channel().remoteAddress().toString()
     * value: public port list
     */
    private Map<String, ArrayList<Integer>> clientMappingPort;
    /**
     * 保存客户端 key 和地址的对应关系
     * key: authKey
     * value: address
     */
    private Map<String, String> clientAddress;

    Connect() {
        channelGroupMap = new ConcurrentHashMap<>();
        publicChannelMap = new ConcurrentHashMap<>();
        publicBindChannel = new ConcurrentHashMap<>();
        clientMap = new ConcurrentHashMap<>();
        clientMappingPort = new ConcurrentHashMap<>();
        clientAddress = new ConcurrentHashMap<>();
        clientInit();
        publicInit();
    }

    public void addChannels(String key, Channel ch) {
        ChannelGroup channels = channelGroupMap.get(key);
        if (channels == null) {
            channels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
            channelGroupMap.put(key, channels);
        }
        channels.add(ch);
    }

    public void addPublicBindChannel(String port, Channel channel) {
        publicBindChannel.put(port, channel);
    }

    public void addPublicChannel(Channel channel) {
        String channelId = channel.id().asLongText();
        publicChannelMap.put(channelId, channel);
    }

    public void clientInit() {
        clientBossGroup = new NioEventLoopGroup();
        clientWorkerGroup = new NioEventLoopGroup();
    }

    public void closeClient(String addressKey) {
        // 关闭与客户端的隧道
        ChannelGroup channels = channelGroupMap.get(addressKey);
        if (channels != null) {
            channels.close();
        }

        // 关闭客服端申请的公开端口
        for (Integer port : clientMappingPort.get(addressKey)) {
            Channel channel = publicBindChannel.get(String.valueOf(port));
            if (channel != null) {
                channel.close();
            }
        }
    }

    public Map<String, ChannelGroup> getChannelGroupMap() {
        return channelGroupMap;
    }

    public ChannelGroup getChannels(String key) {
        return channelGroupMap.get(key);
    }

    public ChannelHandlerContext getClient(String authKey) {
        return clientMap.get(clientAddress.get(authKey));
    }

    public EventLoopGroup getClientBossGroup() {
        return clientBossGroup;
    }

    public EventLoopGroup getClientWorkerGroup() {
        return clientWorkerGroup;
    }

    public EventLoopGroup getPublicBossGroup() {
        return publicBossGroup;
    }

    public Channel getPublicChannel(String channelId) {
        return publicChannelMap.get(channelId);
    }

    public Map<String, Channel> getPublicChannelMap() {
        return publicChannelMap;
    }

    public EventLoopGroup getPublicWorkerGroup() {
        return publicWorkerGroup;
    }

    public void publicChannelClose(String channelId) {
        Channel channel = publicChannelMap.get(channelId);
        if (channel != null) {
            channel.close();
        }
    }

    public void publicInit() {
        if (publicBossGroup != null) {
            publicBossGroup.shutdownGracefully();
        }
        if (publicWorkerGroup != null) {
            publicWorkerGroup.shutdownGracefully();
        }
        publicBossGroup = new NioEventLoopGroup();
        publicWorkerGroup = new NioEventLoopGroup();
    }

    public void putClient(String authKey, String addressKey,
                          ChannelHandlerContext channelHandlerContext,
                          ArrayList<Integer> publicPortList) {
        clientMap.put(addressKey, channelHandlerContext);
        clientMappingPort.put(addressKey, publicPortList);
        clientAddress.put(authKey, addressKey);
    }

    public void removePublicChannel(String channelId) {
        publicChannelMap.remove(channelId);
    }
}
