package com.zyd.sop.gatewaycommon.zuul.result;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zyd.sop.gatewaycommon.bean.SopConstants;
import com.zyd.sop.gatewaycommon.exception.ApiException;
import com.zyd.sop.gatewaycommon.message.Error;
import com.zyd.sop.gatewaycommon.message.ErrorEnum;
import com.zyd.sop.gatewaycommon.result.BaseExecutorAdapter;
import com.zyd.sop.gatewaycommon.zuul.ZuulContext;
import com.netflix.util.Pair;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

/**
 * @author tanghc
 */
@Slf4j
public class ZuulResultExecutor extends BaseExecutorAdapter<RequestContext, String> {

    @Override
    public int getResponseStatus(RequestContext requestContext) {
        List<Pair<String, String>> bizHeaders = requestContext.getZuulResponseHeaders();
        int status = bizHeaders.stream()
                .filter(header -> SopConstants.X_SERVICE_ERROR_CODE.equals(header.first()))
                .map(header -> Integer.valueOf(header.second()))
                .findFirst()
                .orElse(requestContext.getResponseStatusCode());

        return status;
    }

    @Override
    public String getResponseErrorMessage(RequestContext requestContext) {
        List<Pair<String, String>> bizHeaders = requestContext.getZuulResponseHeaders();
        int index = -1;
        String errorMsg = null;
        for (int i = 0; i < bizHeaders.size(); i++) {
            Pair<String, String> header = bizHeaders.get(i);
            if (SopConstants.X_SERVICE_ERROR_MESSAGE.equals(header.first())) {
                errorMsg = header.second();
                index = i;
                break;
            }
        }
        if (index > -1) {
            requestContext.getZuulResponseHeaders().remove(index);
        }
        return errorMsg;
    }

    @Override
    public Map<String, Object> getApiParam(RequestContext requestContext) {
        return ZuulContext.getApiParam();
    }

    @Override
    public String buildErrorResult(RequestContext request, Throwable throwable) {
        Error error = getError(throwable);
        JSONObject jsonObject = (JSONObject) JSON.toJSON(error);
        return this.merge(request, jsonObject);
    }

    public static Error getError(Throwable throwable) {
        Error error = null;
        if (throwable instanceof ZuulException) {
            ZuulException ex = (ZuulException) throwable;
            Throwable cause = ex.getCause();
            if (cause instanceof ApiException) {
                ApiException apiException = (ApiException) cause;
                error = apiException.getError();
            }
        }
        if (error == null) {
            error = ErrorEnum.AOP_UNKNOW_ERROR.getErrorMeta().getError();
        }
        return error;
    }
}
