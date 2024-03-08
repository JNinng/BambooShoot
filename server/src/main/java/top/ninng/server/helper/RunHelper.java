package top.ninng.server.helper;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import top.ninng.common.codec.ProxyMessageDecoder;
import top.ninng.common.codec.ProxyMessageEncoder;
import top.ninng.common.constant.C;
import top.ninng.server.Config;
import top.ninng.server.Connect;
import top.ninng.server.handler.HeartBeatHandler;
import top.ninng.server.handler.ServerHandler;

import java.util.concurrent.TimeUnit;

/**
 * 服务启动帮助类
 *
 * @Author OhmLaw
 * @Date 2024/1/30 15:16
 * @Version 1.0
 */
public class RunHelper {

    /**
     * 数据帧最大长度
     */
    private static final int MAX_FRAME_LENGTH = Integer.MAX_VALUE;
    private static final int LENGTH_FIELD_OFFSET = 0;
    private static final int LENGTH_FIELD_LENGTH = 4;
    private static final int LENGTH_ADJUSTMENT = 0;
    private static final int INITIAL_BYTES_TO_STRIP = 4;
    /**
     * 读超时
     */
    private static final int READER_IDLE_TIME = 60;
    /**
     * 写超时
     */
    private static final int WRITER_IDLE_TIME = 0;
    /**
     * 读/写超时
     */
    private static final int ALL_IDLE_TIME = 0;

    public void start() {
        String host = (String) Config.get(C.LOCALHOST);
        int port = (int) Config.get(C.PORT);

        ChannelInitializer<SocketChannel> channelInitializer = new ChannelInitializer<SocketChannel>() {

            @Override
            protected void initChannel(SocketChannel ch) {
                ch.pipeline().addLast(
                        // 设置读超时为60秒，配合心跳机制处理器使用
                        new IdleStateHandler(READER_IDLE_TIME, WRITER_IDLE_TIME, ALL_IDLE_TIME, TimeUnit.SECONDS),
                        new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP),
                        new ProxyMessageDecoder(),
                        new ProxyMessageEncoder(),
                        new ServerHandler(),
                        // 服务器心跳处理器
                        new HeartBeatHandler()
                );
            }
        };

        BootstrapHelper bootstrapHelper = new BootstrapHelper();
        bootstrapHelper.start(
                Connect.INSTANCE.getPublicBossGroup(),
                Connect.INSTANCE.getPublicWorkerGroup(),
                host,
                port,
                channelInitializer);
    }
}
