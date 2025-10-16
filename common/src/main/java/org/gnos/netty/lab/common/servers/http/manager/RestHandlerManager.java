package org.gnos.netty.lab.common.servers.http.manager;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.ArrayUtil;
import cn.hutool.json.JSONUtil;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.enums.RestMethodType;
import org.gnos.netty.lab.common.servers.http.ParamInfo;
import org.gnos.netty.lab.common.servers.http.controller.HttpController;
import org.gnos.netty.lab.common.servers.http.macher.PathMatcher;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Map;
import java.util.Set;

@Slf4j
@Component
public class RestHandlerManager {

    private final Map<RestMethodType, Map<String, PathMatcher.HandleMethod>> handleMaps = Maps.newConcurrentMap();
    private final Map<String, Set<String>> optionsMap = Maps.newConcurrentMap();
    @Autowired
    PathMatcher pathMatcher;

    /**
     * 获取参数信息
     */
    static ParamInfo[] getParamInfos(Method method) {
        //约定，每个参数都带注解
        Parameter[] parameters = method.getParameters();
        if (ArrayUtil.isEmpty(parameters)) {
            log.info("method:{}, params:{}", method.getName(), "empty");
            return new ParamInfo[0];
        }
        ParamInfo[] ret = new ParamInfo[parameters.length];
        for (int i = 0; i < parameters.length; i++) {
            Annotation anno = parameters[i].getAnnotations()[0];
            ret[i] = new ParamInfo(getAnnotationValue(anno), parameters[i].getType(), anno.annotationType());
        }
        log.info("method:{}, params:{}", method.getName(), JSONUtil.toJsonStr(ret));
        //TODO:check，每个类型唯一检测，每个方法最多一个body，最多一个model
        //TODO:check，QueryVar和QueryModel不可并存
        return ret;
    }

    /**
     * 解析value()
     */
    static String getAnnotationValue(Annotation annotation) {
        try {
            Method m = annotation.annotationType().getDeclaredMethod("value");
            return (String) m.invoke(annotation, null);
        } catch (Exception e) {
            log.error(e.toString(), e);
            //参数解析错误，直接退出
            System.exit(-1);
        }
        return null;
    }

    public void registerHandlerMethod(RestMethodType restMethodType, String path, Method method, HttpController handler) {
        Map<String, PathMatcher.HandleMethod> handleMap = handleMaps.getOrDefault(restMethodType, Maps.newConcurrentMap());
        handleMap.put(path, new PathMatcher.HandleMethod(handler, method, getParamInfos(method)));
        handleMaps.put(restMethodType, handleMap);

        Set<String> set = optionsMap.getOrDefault(path, Sets.newConcurrentHashSet());
        set.add(restMethodType.name());
        optionsMap.put(path, set);
        log.info("register HttpHandler : {} ---> {} : {}", handler.getClass().getSimpleName(), restMethodType, path);
    }

    public Set<String> match(String path) {
        if (CollUtil.isNotEmpty(optionsMap)) {
            for (Map.Entry<String, Set<String>> entry : optionsMap.entrySet()) {
                if (pathMatcher.match(entry.getKey(), path)) {
                    return entry.getValue();
                }
            }
        }
        return Sets.newConcurrentHashSet();
    }

    public PathMatcher.MatchResult match(RestMethodType restMethodType, String path) {
        PathMatcher.MatchResult ret = null;
        Map<String, PathMatcher.HandleMethod> handleMap = handleMaps.get(restMethodType);
        if (CollUtil.isNotEmpty(handleMap)) {
            for (Map.Entry<String, PathMatcher.HandleMethod> entry : handleMap.entrySet()) {
                ret = pathMatcher.match(entry.getKey(), path, entry.getValue());
                if (ret != null) {
                    return ret;
                }
            }
        }
        return ret;
    }

}
