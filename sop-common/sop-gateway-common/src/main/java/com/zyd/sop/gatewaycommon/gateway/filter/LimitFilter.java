package com.zyd.sop.gatewaycommon.gateway.filter;

import com.zyd.sop.gatewaycommon.bean.ApiConfig;
import com.zyd.sop.gatewaycommon.bean.ConfigLimitDto;
import com.zyd.sop.gatewaycommon.exception.ApiException;
import com.zyd.sop.gatewaycommon.gateway.GatewayContext;
import com.zyd.sop.gatewaycommon.limit.LimitManager;
import com.zyd.sop.gatewaycommon.limit.LimitType;
import com.zyd.sop.gatewaycommon.manager.LimitConfigManager;
import com.zyd.sop.gatewaycommon.message.ErrorImpl;
import com.zyd.sop.gatewaycommon.param.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

/**
 * @author tanghc
 */
@Slf4j
public class LimitFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ApiConfig apiConfig = ApiConfig.getInstance();
        // 限流功能未开启，直接返回
        if (!apiConfig.isOpenLimit()) {
            return chain.filter(exchange);
        }
        ApiParam apiParam = GatewayContext.getApiParam(exchange);
        ConfigLimitDto configLimitDto = this.findConfigLimitDto(apiConfig, apiParam, exchange);
        if (configLimitDto == null) {
            return null;
        }
        // 单个限流功能未开启
        if (configLimitDto.getLimitStatus() == ConfigLimitDto.LIMIT_STATUS_CLOSE) {
            return null;
        }
        byte limitType = configLimitDto.getLimitType().byteValue();
        LimitManager limitManager = ApiConfig.getInstance().getLimitManager();
        // 如果是漏桶策略
        if (limitType == LimitType.LEAKY_BUCKET.getType()) {
            boolean acquire = limitManager.acquire(configLimitDto);
            if (!acquire) {
                throw new ApiException(new ErrorImpl(configLimitDto.getLimitCode(), configLimitDto.getLimitMsg()));
            }
        } else if (limitType == LimitType.TOKEN_BUCKET.getType()) {
            limitManager.acquireToken(configLimitDto);
        }
        return chain.filter(exchange);
    }

    @Override
    public int getOrder() {
        return Orders.LIMIT_ORDER;
    }

    protected ConfigLimitDto findConfigLimitDto(ApiConfig apiConfig, ApiParam apiParam, ServerWebExchange exchange) {
        LimitConfigManager limitConfigManager = apiConfig.getLimitConfigManager();

        String routeId = apiParam.fetchNameVersion();
        String appKey = apiParam.fetchAppKey();
        String ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();

        String[] limitKeys = new String[]{
                routeId,
                appKey,
                routeId + appKey,

                ip + routeId,
                ip + appKey,
                ip + routeId + appKey,
        };

        List<ConfigLimitDto> limitConfigList = new ArrayList<>();
        for (String limitKey : limitKeys) {
            ConfigLimitDto configLimitDto = limitConfigManager.get(limitKey);
            limitConfigList.add(configLimitDto);
        }
        if (limitConfigList.isEmpty()) {
            return null;
        }
        Collections.sort(limitConfigList, Comparator.comparing(ConfigLimitDto::getOrderIndex));
        return limitConfigList.get(0);
    }
}
