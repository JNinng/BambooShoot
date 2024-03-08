package top.ninng.common.codec;

import com.fasterxml.jackson.core.type.TypeReference;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.ByteBufUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToMessageDecoder;
import io.netty.util.CharsetUtil;
import top.ninng.common.protocol.ProxyMessage;
import top.ninng.common.utils.ObjectMapper;

import java.util.List;
import java.util.Map;

/**
 * 代理消息解码器
 *
 * @Author OhmLaw
 * @Date 2024/1/28 10:09
 * @Version 1.0
 */
public class ProxyMessageDecoder extends MessageToMessageDecoder<ByteBuf> {

    @Override
    protected void decode(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf, List<Object> list) throws Exception {
        short type = byteBuf.readShort();

        int metaDataLength = byteBuf.readInt();
        CharSequence metaDataString = byteBuf.readCharSequence(metaDataLength, CharsetUtil.UTF_8);
        Map<String, Object> metaData = ObjectMapper.readValue(metaDataString.toString(),
                new TypeReference<Map<String, Object>>() {
                });

        byte[] data = null;
        if (byteBuf.isReadable()) {
            data = ByteBufUtil.getBytes(byteBuf);
        }

        ProxyMessage proxyMessage = new ProxyMessage();
        proxyMessage.setType(type);
        proxyMessage.setMetaData(metaData);
        proxyMessage.setData(data);

        list.add(proxyMessage);
    }
}
