package com.zyd.sop.gatewaycommon.gateway.result;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.zyd.sop.gatewaycommon.bean.SopConstants;
import com.zyd.sop.gatewaycommon.exception.ApiException;
import com.zyd.sop.gatewaycommon.message.Error;
import com.zyd.sop.gatewaycommon.message.ErrorEnum;
import com.zyd.sop.gatewaycommon.result.BaseExecutorAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.support.NotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;

import java.util.List;
import java.util.Map;


/**
 * @author tanghc
 */
@Slf4j
public class GatewayResultExecutor extends BaseExecutorAdapter<ServerWebExchange, GatewayResult> {

    @Override
    public int getResponseStatus(ServerWebExchange exchange) {
        int responseStatus = HttpStatus.OK.value();
        List<String> errorCodeList = exchange.getResponse().getHeaders().get(SopConstants.X_SERVICE_ERROR_CODE);
        if (!CollectionUtils.isEmpty(errorCodeList)) {
            String errorCode = errorCodeList.get(0);
            responseStatus = Integer.valueOf(errorCode);
        }
        return responseStatus;
    }

    @Override
    public String getResponseErrorMessage(ServerWebExchange exchange) {
        String errorMsg = null;
        List<String> errorMessageList = exchange.getResponse().getHeaders().get(SopConstants.X_SERVICE_ERROR_MESSAGE);
        if (!CollectionUtils.isEmpty(errorMessageList)) {
            errorMsg = errorMessageList.get(0);
        }
        exchange.getResponse().getHeaders().remove(SopConstants.X_SERVICE_ERROR_MESSAGE);
        return errorMsg;
    }

    @Override
    public Map<String, Object> getApiParam(ServerWebExchange exchange) {
        return exchange.getAttribute(SopConstants.CACHE_REQUEST_BODY_FOR_MAP);
    }

    @Override
    public GatewayResult buildErrorResult(ServerWebExchange exchange, Throwable ex) {
        Error error = null;
        if (ex instanceof ApiException) {
            ApiException apiException = (ApiException) ex;
            error = apiException.getError();
        } else if (ex instanceof NotFoundException) {
            error = ErrorEnum.ISV_INVALID_METHOD.getErrorMeta().getError();
        } else if (ex instanceof ResponseStatusException) {
            ResponseStatusException responseStatusException = (ResponseStatusException) ex;
            HttpStatus status = responseStatusException.getStatus();
            if (status == HttpStatus.NOT_FOUND) {
                error = ErrorEnum.ISV_INVALID_METHOD.getErrorMeta().getError();
            }
        }

        if (error == null) {
            error = ErrorEnum.AOP_UNKNOW_ERROR.getErrorMeta().getError();
        }

        JSONObject jsonObject = (JSONObject) JSON.toJSON(error);
        String body = this.merge(exchange, jsonObject);

        return new GatewayResult(HttpStatus.OK, MediaType.APPLICATION_JSON_UTF8, body);
    }

}
