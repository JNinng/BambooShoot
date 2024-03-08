package top.ninng.client.helper;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.socket.nio.NioSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * {@link Bootstrap} 启动帮助类
 *
 * @Author OhmLaw
 * @Date 2024/1/28 14:29
 * @Version 1.0
 */
public class BootStrapHelper {

    private static final Logger logger = LoggerFactory.getLogger(BootStrapHelper.class);

    private Channel channel;

    public synchronized void close() {
        if (channel != null) {
            channel.close();
        }
    }

    public synchronized void start(EventLoopGroup group, ChannelInitializer<Channel> channelInitializer,
                                   String host, int port) {
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .option(ChannelOption.SO_KEEPALIVE, true)
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 2000)
                .handler(channelInitializer);
        try {
            channel = bootstrap.connect(host, port).sync().channel();
            channel.closeFuture().addListener(future -> {
                channel.deregister();
                channel.close();
            });
        } catch (InterruptedException e) {
            close();
            group.shutdownGracefully();
            logger.error(e.toString());
        }
    }
}
