package com.zyd.sop.gatewaycommon.gateway.route;

import lombok.Data;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author tanghc
 */
@Data
public class GatewayPredicateDefinition {
    /** 断言对应的Name */
    private String name;
    /** 配置的断言规则 */
    private Map<String, String> args = new LinkedHashMap<>();
}
