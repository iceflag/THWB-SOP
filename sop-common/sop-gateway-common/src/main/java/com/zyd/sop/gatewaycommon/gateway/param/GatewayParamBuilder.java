package com.zyd.sop.gatewaycommon.gateway.param;

import com.zyd.sop.gatewaycommon.gateway.GatewayContext;
import com.zyd.sop.gatewaycommon.param.BaseParamBuilder;
import org.springframework.web.server.ServerWebExchange;

import java.util.Collections;
import java.util.Map;

/**
 * @author tanghc
 */
public class GatewayParamBuilder extends BaseParamBuilder<ServerWebExchange> {

    @Override
    public Map<String, String> buildRequestParams(ServerWebExchange exchange) {
        Map<String, String> params = GatewayContext.getRequestParams(exchange);
        return params == null ? Collections.emptyMap() : params;
    }

    @Override
    public String getIP(ServerWebExchange ctx) {
        return ctx.getRequest().getRemoteAddress().getAddress().getHostAddress();
    }
}
