package com.zyd.sop.gatewaycommon.gateway.route;

import com.zyd.sop.gatewaycommon.bean.SopConstants;
import com.zyd.sop.gatewaycommon.gateway.GatewayContext;
import com.zyd.sop.gatewaycommon.param.ParamNames;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.handler.predicate.AbstractRoutePredicateFactory;
import org.springframework.util.CollectionUtils;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.server.ServerWebExchange;

import javax.validation.constraints.NotEmpty;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * 此断言决定执行哪个路由
 *
 * @author tanghc
 */
@Slf4j
public class NameVersionRoutePredicateFactory extends AbstractRoutePredicateFactory<NameVersionRoutePredicateFactory.Config> {

    private static final String PARAM_KEY = "param";
    private static final String REGEXP_KEY = "regexp";

    public NameVersionRoutePredicateFactory() {
        super(Config.class);
    }

    @Override
    public List<String> shortcutFieldOrder() {
        return Arrays.asList(PARAM_KEY, REGEXP_KEY);
    }

    /**
     * config.param为nameVersion，即路由id
     *
     * @param config
     * @return 返回断言
     */
    @Override
    public Predicate<ServerWebExchange> apply(Config config) {

        return exchange -> {
            Map<String, String> params = GatewayContext.getRequestParams(exchange);
            if (CollectionUtils.isEmpty(params)) {
                return false;
            }
            String nameVersion = config.param;
            String name = params.getOrDefault(ParamNames.API_NAME, SopConstants.UNKNOWN_METHOD);
            String version = params.getOrDefault(ParamNames.VERSION_NAME, "");
            return (name + version).equals(nameVersion);
        };
    }

    @Validated
    public static class Config {
        @NotEmpty
        private String param;

        private String regexp;

        public String getParam() {
            return param;
        }

        public Config setParam(String param) {
            this.param = param;
            return this;
        }

        public String getRegexp() {
            return regexp;
        }

        public Config setRegexp(String regexp) {
            this.regexp = regexp;
            return this;
        }
    }
}
