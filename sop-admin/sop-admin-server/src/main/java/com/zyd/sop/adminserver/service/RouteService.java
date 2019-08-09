package com.zyd.sop.adminserver.service;

import com.alibaba.fastjson.JSON;
import com.zyd.sop.adminserver.api.service.param.RouteSearchParam;
import com.zyd.sop.adminserver.bean.GatewayRouteDefinition;
import com.zyd.sop.adminserver.bean.ZookeeperContext;
import org.apache.commons.lang.StringUtils;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

import static java.util.stream.Collectors.toList;

/**
 * @author tanghc
 */
@Service
public class RouteService {

    public List<GatewayRouteDefinition> getRouteDefinitionList(RouteSearchParam param) throws Exception {
        if (StringUtils.isBlank(param.getServiceId())) {
            return Collections.emptyList();
        }

        String searchPath = ZookeeperContext.getSopRouteRootPath() + "/" + param.getServiceId();

        List<ChildData> childDataList = ZookeeperContext.getChildrenData(searchPath);

        List<GatewayRouteDefinition> routeDefinitionStream = childDataList.stream()
                .map(childData -> {
                    String serviceNodeData = new String(childData.getData());
                    GatewayRouteDefinition routeDefinition = JSON.parseObject(serviceNodeData, GatewayRouteDefinition.class);
                    return routeDefinition;
                })
                .filter(gatewayRouteDefinition -> {
                    boolean isRoute = gatewayRouteDefinition.getOrder() != Integer.MIN_VALUE;
                    String id = param.getId();
                    if (StringUtils.isBlank(id)) {
                        return isRoute;
                    } else {
                        return isRoute && gatewayRouteDefinition.getId().contains(id);
                    }
                })
                .collect(toList());

        return routeDefinitionStream;
    }

}
