package top.ninng.client.helper;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;
import io.netty.handler.timeout.IdleStateHandler;
import top.ninng.client.Config;
import top.ninng.client.Connect;
import top.ninng.client.handler.ClientHandler;
import top.ninng.client.handler.HeartBeatHandler;
import top.ninng.common.codec.ProxyMessageDecoder;
import top.ninng.common.codec.ProxyMessageEncoder;
import top.ninng.common.constant.C;

import java.util.concurrent.TimeUnit;

/**
 * 客户端启动帮助类
 *
 * @Author OhmLaw
 * @Date 2024/1/28 14:29
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
    private static final int READER_IDLE_TIME = 0;
    /**
     * 写超时
     */
    private static final int WRITER_IDLE_TIME = 40;
    /**
     * 读/写超时
     */
    private static final int ALL_IDLE_TIME = 0;

    public void start() {
        String serverHost = (String) Config.get(C.SERVER_HOST);
        int serverPort = (int) Config.get(C.SERVER_PORT);
        ChannelInitializer<Channel> channelInitializer = new ChannelInitializer<Channel>() {
            @Override
            protected void initChannel(Channel channel) {
                channel.pipeline().addLast(
                        // 设置写超时为40秒，配合心跳机制处理器使用
                        new IdleStateHandler(READER_IDLE_TIME, WRITER_IDLE_TIME, ALL_IDLE_TIME, TimeUnit.SECONDS),
                        new LengthFieldBasedFrameDecoder(MAX_FRAME_LENGTH, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH, LENGTH_ADJUSTMENT, INITIAL_BYTES_TO_STRIP),
                        new ProxyMessageDecoder(),
                        new ProxyMessageEncoder(),
                        new ClientHandler(),
                        // 客户端心跳处理器
                        new HeartBeatHandler()
                );
            }
        };
        BootStrapHelper clientBootStrapHelper = new BootStrapHelper();
        clientBootStrapHelper.start(Connect.INSTANCE.getWorkerGroup(), channelInitializer, serverHost, serverPort);
    }
}
