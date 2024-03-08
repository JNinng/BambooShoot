package top.ninng.common.protocol;

import java.util.Arrays;
import java.util.Map;

/**
 * 代理传输消息协议
 *
 * @Author OhmLaw
 * @Date 2024/1/28 10:11
 * @Version 1.0
 */
public class ProxyMessage {

    /**
     * 注册
     */
    public static final short TYPE_REGISTER = 0;
    /**
     * 授权
     */
    public static final short TYPE_AUTH = 1;
    /**
     * 建立连接
     */
    public static final short TYPE_CONNECTED = 2;
    /**
     * 断开连接
     */
    public static final short TYPE_DISCONNECTED = 3;
    /**
     * 心跳
     */
    public static final short TYPE_KEEPALIVE = 4;
    /**
     * 数据传输
     */
    public static final short TYPE_DATA = 5;

    /**
     * 消息类型
     */
    private short type;
    /**
     * 元数据：id,key...
     */
    private Map<String, Object> metaData;
    /**
     * 消息内容
     */
    private byte[] data;

    public ProxyMessage() {
    }

    public ProxyMessage(short type) {
        this.type = type;
    }

    @Override
    public String toString() {
        return "ProxyMessage{" +
                "type=" + type +
                ", metaData=" + metaData +
                ", data=" + Arrays.toString(data) +
                '}';
    }

    public static String typeToString(short type) {
        String typeString = "";
        switch (type) {
            case TYPE_REGISTER:
                typeString = "REGISTER";
                break;
            case TYPE_AUTH:
                typeString = "AUTH";
                break;
            case TYPE_CONNECTED:
                typeString = "CONNECTED";
                break;
            case TYPE_DISCONNECTED:
                typeString = "DISCONNECTED";
                break;
            case TYPE_DATA:
                typeString = "DATA";
                break;
        }
        return typeString;
    }

    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    public Map<String, Object> getMetaData() {
        return metaData;
    }

    public void setMetaData(Map<String, Object> metaData) {
        this.metaData = metaData;
    }

    public short getType() {
        return type;
    }

    public void setType(short type) {
        this.type = type;
    }
}
