package top.ninng.common.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.exc.StreamReadException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DatabindException;

import java.io.IOException;

/**
 * JSON 序列化
 *
 * @Author OhmLaw
 * @Date 2024/1/28 11:15
 * @Version 1.0
 */
public class ObjectMapper {

    private static com.fasterxml.jackson.databind.ObjectMapper objectMapper;

    static {
        init();
    }

    private static void init() {
        if (objectMapper == null) {
            objectMapper = new com.fasterxml.jackson.databind.ObjectMapper();
        }
    }

    public static <T> T readValue(String content, TypeReference<T> valueTypeRef) throws IOException, StreamReadException,
            DatabindException {
        init();
        return objectMapper.readValue(content, valueTypeRef);
    }

    public static String writeValueAsString(Object value) throws JsonProcessingException {
        init();
        return objectMapper.writeValueAsString(value);
    }
}
