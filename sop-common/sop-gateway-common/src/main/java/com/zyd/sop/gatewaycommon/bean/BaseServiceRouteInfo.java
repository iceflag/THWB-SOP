package com.zyd.sop.gatewaycommon.bean;

import lombok.Data;

import java.util.Collections;
import java.util.List;

/**
 * @author tanghc
 */
@Data
public class BaseServiceRouteInfo<T extends BaseRouteDefinition> {
    private String serviceId;
    private List<T> routeDefinitionList = Collections.emptyList();
}
