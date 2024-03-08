package top.ninng.server.helper;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import top.ninng.server.Connect;

/**
 * @Author OhmLaw
 * @Date 2024/1/30 15:45
 * @Version 1.0
 */
public class BootstrapHelper {

    private final Logger logger = LoggerFactory.getLogger(BootstrapHelper.class);

    public synchronized void start(EventLoopGroup bossGroup, EventLoopGroup workerGroup,
                                   String host, int port,
                                   ChannelInitializer<? extends Channel> channelInitializer) {
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap
                .group(bossGroup, workerGroup)
                .channel(NioServerSocketChannel.class)
                .childHandler(channelInitializer)
                .childOption(ChannelOption.SO_KEEPALIVE, true);
        try {
            Channel channel = serverBootstrap.bind(host, port).sync().channel();
            channel.closeFuture().addListener(future -> {
                channel.deregister();
                channel.close();
            });
            Connect.INSTANCE.addPublicBindChannel(String.valueOf(port), channel);
        } catch (InterruptedException e) {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
            logger.error(e.toString());
        }
    }
}
