package com.zyd.sop.gatewaycommon.gateway.route;

import com.zyd.sop.gatewaycommon.manager.BaseRouteManager;
import com.zyd.sop.gatewaycommon.manager.RouteRepository;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.cloud.gateway.filter.FilterDefinition;
import org.springframework.cloud.gateway.handler.predicate.PredicateDefinition;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.core.env.Environment;

import java.net.URI;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * 从zookeeper监听route信息
 *
 * @author tanghc
 */
@Getter
@Slf4j
public class GatewayZookeeperRouteManager extends BaseRouteManager<GatewayServiceRouteInfo, GatewayRouteDefinition, GatewayTargetRoute> {

    public GatewayZookeeperRouteManager(Environment environment, RouteRepository<GatewayTargetRoute> routeRepository) {
        super(environment, routeRepository);
    }

    @Override
    protected Class<GatewayServiceRouteInfo> getServiceRouteInfoClass() {
        return GatewayServiceRouteInfo.class;
    }

    @Override
    protected Class<GatewayRouteDefinition> getRouteDefinitionClass() {
        return GatewayRouteDefinition.class;
    }

    @Override
    protected GatewayTargetRoute buildRouteDefinition(GatewayServiceRouteInfo serviceRouteInfo, GatewayRouteDefinition gatewayRouteDefinition) {
        RouteDefinition routeDefinition = new RouteDefinition();
        routeDefinition.setId(gatewayRouteDefinition.getId());
        routeDefinition.setUri(URI.create(gatewayRouteDefinition.getUri() + "#" + gatewayRouteDefinition.getPath()));
        routeDefinition.setOrder(gatewayRouteDefinition.getOrder());
        List<FilterDefinition> filterDefinitionList = new ArrayList<>(gatewayRouteDefinition.getFilters().size());
        LinkedList<PredicateDefinition> predicateDefinitionList = new LinkedList<>();
        for (GatewayFilterDefinition filter : gatewayRouteDefinition.getFilters()) {
            FilterDefinition filterDefinition = new FilterDefinition();
            BeanUtils.copyProperties(filter, filterDefinition);
            filterDefinitionList.add(filterDefinition);
        }

        for (GatewayPredicateDefinition predicate : gatewayRouteDefinition.getPredicates()) {
            PredicateDefinition predicateDefinition = new PredicateDefinition();
            BeanUtils.copyProperties(predicate, predicateDefinition);
            predicateDefinitionList.add(predicateDefinition);
        }
        this.addPredicate(predicateDefinitionList, "NameVersion", gatewayRouteDefinition.getId());
        this.addPredicate(predicateDefinitionList, "ReadBody", "");
        routeDefinition.setFilters(filterDefinitionList);
        routeDefinition.setPredicates(predicateDefinitionList);
        return new GatewayTargetRoute(serviceRouteInfo, gatewayRouteDefinition, routeDefinition);
    }

    /**
     * 添加断言
     *
     * @param predicateDefinitionList
     * @param name                    断言名称
     * @param args                    断言参数
     */
    protected void addPredicate(LinkedList<PredicateDefinition> predicateDefinitionList, String name, String args) {
        for (PredicateDefinition predicateDefinition : predicateDefinitionList) {
            // 如果已经存在，直接返回
            if (predicateDefinition.getName().equals(name)) {
                return;
            }
        }
        predicateDefinitionList.addFirst(new PredicateDefinition(name + "=" + args));
    }

}
