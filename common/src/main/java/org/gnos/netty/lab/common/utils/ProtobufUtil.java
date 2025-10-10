package org.gnos.netty.lab.common.utils;

import cn.hutool.core.util.ClassUtil;
import cn.hutool.core.util.ReflectUtil;
import com.google.protobuf.GeneratedMessageV3;
import com.google.protobuf.Message;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ProtobufUtil {

    static final String delimiter = "_";
    static final String reqTag = "Req_";
    static final String respTag = "Resp_";
    static Map<Short, Class<?>> reqClasses = new ConcurrentHashMap<>();
    static Map<Short, Class<?>> respClasses = new ConcurrentHashMap<>();


    @Value("${netty.proto.packageName:org.gnos.netty.lab.proto.client}")
    String packageName;

    public static short getOpCode(Message message) {
        try {
            String simpleName = message.getClass().getSimpleName();
            if (simpleName.contains(delimiter)) {
                return Short.parseShort(simpleName.substring(simpleName.indexOf(delimiter) + delimiter.length()));
            }
        } catch (Exception e) {
            log.error(e.toString(), e);
        }
        return 0;
    }

    public static Class<?> getReqClass(short opcode) {
        return reqClasses.get(opcode);
    }

    public static Class<?> getRespClass(short opcode) {
        return respClasses.get(opcode);
    }

    public static Message fromBytes(Class<?> clazz, byte[] bytes) {
        Method method = ReflectUtil.getMethod(clazz, "parseFrom", byte[].class);
        return ReflectUtil.invokeStatic(method, bytes);
    }

    @PostConstruct
    public void loadProtoClasses() {
        log.info("proto load start...");
        Set<Class<?>> classes = ClassUtil.scanPackageBySuper(packageName, GeneratedMessageV3.class);
        classes.forEach(c -> {
            if (c.getSimpleName().contains(delimiter)) {
                try {
                    short opcode = Short.parseShort(c.getSimpleName().substring(c.getSimpleName().indexOf(delimiter) + delimiter.length()));
                    if (c.getSimpleName().contains(reqTag)) {
                        reqClasses.put(opcode, c);
                    } else if (c.getSimpleName().contains(respTag)) {
                        respClasses.put(opcode, c);
                    } else {
                        reqClasses.put(opcode, c);
                        respClasses.put(opcode, c);
                    }
                } catch (Exception e) {
                    log.error(e.toString(), e);
                    System.exit(-1);
                }
            }
        });
        log.info("proto load finish size:{},{}", reqClasses.size(), respClasses.size());
    }

}
