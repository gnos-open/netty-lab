package org.gnos.netty.lab.common.servers.http.macher;

import org.gnos.netty.lab.common.servers.http.ParamInfo;
import org.gnos.netty.lab.common.servers.http.controller.HttpController;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 请求路径匹配，支持PathVar匹配
 */
@Component
public class PathMatcher {

    // 缓存已编译的正则表达式模式
    private final Map<String, PatternInfo> patternCache = new HashMap<>();

    /**
     * 只匹配路径，用于options方法返回
     */
    public boolean match(String pattern, String path) {
        if (pattern == null || path == null) {
            return false;
        }
        // 获取或编译模式信息
        PatternInfo patternInfo = patternCache.computeIfAbsent(pattern, this::compileComplexPattern);

        // 进行匹配
        Matcher matcher = patternInfo.regexPattern().matcher(path);
        return matcher.matches();
    }

    /**
     * 匹配路径并提取路径变量
     *
     * @param pattern 路径模式，如 "/users/{id}/posts/{postId}"
     * @param path    实际路径，如 "/users/123/posts/456"
     * @return 匹配结果
     */
    public MatchResult match(String pattern, String path, HandleMethod handleMethod) {
        if (pattern == null || path == null || handleMethod == null) {
            return null;
        }

        // 获取或编译模式信息
        PatternInfo patternInfo = patternCache.computeIfAbsent(pattern, this::compileComplexPattern);

        // 进行匹配
        Matcher matcher = patternInfo.regexPattern().matcher(path);
        if (!matcher.matches()) {
            return null;
        }

        // 提取路径变量
        Map<String, String> pathVariables = new HashMap<>();
        List<String> variableNames = patternInfo.variableNames();

        for (int i = 0; i < variableNames.size(); i++) {
            String variableName = variableNames.get(i);
            String variableValue = matcher.group(i + 1); // group(0)是整个匹配，所以从1开始
            pathVariables.put(variableName, variableValue);
        }

        return new MatchResult(pathVariables, handleMethod);
    }

    /**
     * 编译路径模式为正则表达式
     */
    private PatternInfo compilePattern(String pattern) {
        List<String> variableNames = new ArrayList<>();
        StringBuilder regexBuilder = new StringBuilder();

        // 添加路径开始锚点
        regexBuilder.append("^");

        String[] segments = pattern.split("/");
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            regexBuilder.append("/");

            if (segment.startsWith("{") && segment.endsWith("}")) {
                // 这是一个路径变量
                String variableName = segment.substring(1, segment.length() - 1);
                variableNames.add(variableName);
                regexBuilder.append("([^/]+)"); // 匹配除了斜杠之外的任何字符
            } else {
                // 普通文本段
                regexBuilder.append(Pattern.quote(segment));
            }
        }

        // 添加路径结束锚点
        regexBuilder.append("$");

        Pattern regexPattern = Pattern.compile(regexBuilder.toString());
        return new PatternInfo(regexPattern, variableNames);
    }

    /*支持正则约束，如：{id:\\d+}*/
    private PatternInfo compileComplexPattern(String pattern) {
        List<String> variableNames = new ArrayList<>();
        StringBuilder regexBuilder = new StringBuilder();

        regexBuilder.append("^");

        String[] segments = pattern.split("/");
        for (String segment : segments) {
            if (segment.isEmpty()) {
                continue;
            }

            regexBuilder.append("/");

            if (segment.startsWith("{") && segment.endsWith("}")) {
                String content = segment.substring(1, segment.length() - 1);
                String[] parts = content.split(":", 2);

                String variableName = parts[0];
                String regex = (parts.length > 1) ? parts[1] : "[^/]+";

                variableNames.add(variableName);
                regexBuilder.append("(").append(regex).append(")");
            } else {
                regexBuilder.append(Pattern.quote(segment));
            }
        }

        regexBuilder.append("$");

        Pattern regexPattern = Pattern.compile(regexBuilder.toString());
        return new PatternInfo(regexPattern, variableNames);
    }

    public record HandleMethod(HttpController handler, Method method, ParamInfo[] paramInfos) {
    }

    /**
     * 路径匹配结果
     */
    public record MatchResult(Map<String, String> pathVariables, HandleMethod handleMethod) {
        public String getPathVariable(String name) {
            return pathVariables.get(name);
        }
    }

    /**
     * 将路径模式转换为正则表达式并提取变量名
     */
    private record PatternInfo(Pattern regexPattern, List<String> variableNames) {
    }

}