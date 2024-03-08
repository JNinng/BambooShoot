package top.ninng.common.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;
import io.netty.util.CharsetUtil;
import top.ninng.common.protocol.ProxyMessage;
import top.ninng.common.utils.ObjectMapper;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;

/**
 * 代理消息编码器
 *
 * @Author OhmLaw
 * @Date 2024/1/28 10:37
 * @Version 1.0
 */
public class ProxyMessageEncoder extends MessageToByteEncoder<ProxyMessage> {

    @Override
    protected void encode(ChannelHandlerContext ctx, ProxyMessage proxyMessage, ByteBuf out) throws Exception {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        DataOutputStream dataOutputStream = new DataOutputStream(byteArrayOutputStream);

        short type = proxyMessage.getType();
        dataOutputStream.writeShort(type);

        String metaDataJson = ObjectMapper.writeValueAsString(proxyMessage.getMetaData());
        byte[] metaDataBytes = metaDataJson.getBytes(CharsetUtil.UTF_8);
        dataOutputStream.writeInt(metaDataBytes.length);
        dataOutputStream.write(metaDataBytes);

        if (proxyMessage.getData() != null && proxyMessage.getData().length > 0) {
            dataOutputStream.write(proxyMessage.getData());
        }

        byte[] data = byteArrayOutputStream.toByteArray();
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
