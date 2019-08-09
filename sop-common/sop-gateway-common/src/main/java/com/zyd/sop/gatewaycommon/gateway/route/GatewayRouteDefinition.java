package com.zyd.sop.gatewaycommon.gateway.route;

import com.zyd.sop.gatewaycommon.bean.BaseRouteDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author tanghc
 */
@Getter
@Setter
public class GatewayRouteDefinition extends BaseRouteDefinition {
    /** 路由断言集合配置 */
    private List<GatewayPredicateDefinition> predicates = new ArrayList<>();
    /** 路由过滤器集合配置 */
    private List<GatewayFilterDefinition> filters = new ArrayList<>();
}
