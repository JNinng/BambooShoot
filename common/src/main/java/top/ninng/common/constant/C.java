package top.ninng.common.constant;

/**
 * 键值对相关常量，配置和 Map 相关键，
 * 值期待类型默认为 {@link String}
 *
 * @Author OhmLaw
 * @Date 2024/1/29 13:14
 * @Version 1.0
 */
public class C {

    /**
     * 服务端地址
     */
    public static final String SERVER_HOST = "server-host";
    /**
     * 服务端端口，值期待类型：int
     */
    public static final String SERVER_PORT = "server-port";
    /**
     * 客户端端口，值期待类型：int
     */
    public static final String CLIENT_PORT = "client-port";
    /**
     * 代理类型
     */
    public static final String PROXY_TYPE = "proxy-type";
    /**
     * 描述
     */
    public static final String DESCRIPTION = "description";
    /**
     * 本地地址
     */
    public static final String LOCALHOST = "localhost";
    /**
     * 本地端口，值期待类型：int
     */
    public static final String PORT = "port";
    /**
     * 公开端口列表，值期待类型：{@link java.util.List<int>}
     */
    public static final String PUBLIC_PORTS = "public-ports";
    /**
     * 公开端口，值期待类型：int
     */
    public static final String PUBLIC_PORT = "public-port";
    /**
     * 端口映射关系，值期待类型：{@link java.util.List<java.util.Map<String,Object>>}
     */
    public static final String PORT_MAPPING = "port-mapping";
    /**
     * key
     */
    public static final String AUTH_KEY = "auth-key";
    /**
     * 授权结果，值期待类型：{@link Boolean}
     */
    public static final String IS_AUTH = "is-auth";
    /**
     * 授权相关返回信息，可能的错误信息
     */
    public static final String AUTH_RESULT = "auth-result";
    /**
     * 通道 id
     */
    public static final String CHANNEL_ID = "channel-id";
}
