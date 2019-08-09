package com.zyd.sop.gatewaycommon.zuul.configuration;

import com.zyd.sop.gatewaycommon.bean.ApiContext;
import com.zyd.sop.gatewaycommon.result.ResultExecutor;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.servlet.error.ErrorController;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletResponse;

/**
 * 处理网关自身异常
 *
 * @author tanghc
 */
@Controller
@Slf4j
public class ZuulErrorController implements ErrorController {

    public static final String ERROR_PATH = "/error";

    /**
     * 错误最终会到这里来
     */
    @RequestMapping(ERROR_PATH)
    @ResponseBody
    public Object error(HttpServletResponse response) {
        RequestContext ctx = RequestContext.getCurrentContext();
        if (ctx.getResponse() == null) {
            ctx.setResponse(response);
        }
        ctx.setResponseStatusCode(HttpStatus.OK.value());
        Throwable throwable = ctx.getThrowable();
        return this.buildResult(throwable);
    }

    protected Object buildResult(Throwable throwable) {
        ResultExecutor<RequestContext, String> resultExecutor = ApiContext.getApiConfig().getZuulResultExecutor();
        return resultExecutor.buildErrorResult(RequestContext.getCurrentContext(), throwable);
    }

    @Override
    public String getErrorPath() {
        return ERROR_PATH;
    }
}
