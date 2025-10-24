package org.gnos.netty.lab.server.handler;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import lombok.extern.slf4j.Slf4j;
import org.gnos.netty.lab.common.servers.http.RestResult;
import org.gnos.netty.lab.common.servers.http.anno.*;
import org.gnos.netty.lab.common.servers.http.controller.AbstractHttpController;
import org.gnos.netty.lab.server.model.TestBody;
import org.gnos.netty.lab.server.model.TestModel;
import org.springframework.stereotype.Component;

import java.util.Map;

import static org.gnos.netty.lab.common.enums.RestMethodType.*;

@Slf4j
@Component
@RestController("/test")
public class TestHttpController extends AbstractHttpController {

    @RestMethod(method = GET)
    public RestResult<String> test1() {
        return RestResult.success("success");
    }

    @RestMethod(value = "/{id}", method = GET)
    public RestResult test2(@PathVar("id") String id, @QueryVar("level") Integer level, @QueryVar("age") Integer age) {
        return RestResult.success(new JSONObject().set("id", id).set("level", level).set("age", age));
    }

    @RestMethod(value = "/{id}/model", method = GET)
    public RestResult<TestModel> test3(@PathVar("id") String id, @QueryModel("model") TestModel model) {
        return RestResult.success(model);
    }

    @RestMethod(method = POST)
    public RestResult<String> test4() {
        return RestResult.success("test4");
    }

    @RestMethod(value = "/body", method = POST)
    public RestResult<TestBody> test5(@HeaderMap("header") Map<String, String> header, @BodyJson("body") TestBody body) {
        log.info("header:{}", JSONUtil.toJsonStr(header));
        return RestResult.success(body);
    }

    @RestMethod(method = PUT)
    public RestResult<TestBody> test6(@BodyJson("body") TestBody body) {
        return RestResult.success(body);
    }

}
