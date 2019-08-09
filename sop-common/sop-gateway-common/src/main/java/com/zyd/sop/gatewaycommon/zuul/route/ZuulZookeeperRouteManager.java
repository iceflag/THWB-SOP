package com.zyd.sop.gatewaycommon.zuul.route;

import com.zyd.sop.gatewaycommon.manager.BaseRouteManager;
import com.zyd.sop.gatewaycommon.manager.RouteRepository;
import com.zyd.sop.gatewaycommon.util.RouteUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.netflix.zuul.filters.Route;
import org.springframework.core.env.Environment;

/**
 * 路由内容管理，新增活修改路由
 *
 * @author tanghc
 */
@Slf4j
public class ZuulZookeeperRouteManager extends BaseRouteManager<ZuulServiceRouteInfo, ZuulRouteDefinition, ZuulTargetRoute> {

    /** 路由重试 */
    public static final boolean RETRYABLE = true;

    public ZuulZookeeperRouteManager(Environment environment, RouteRepository<ZuulTargetRoute> routeRepository) {
        super(environment, routeRepository);
    }

    @Override
    protected Class<ZuulServiceRouteInfo> getServiceRouteInfoClass() {
        return ZuulServiceRouteInfo.class;
    }

    @Override
    protected Class<ZuulRouteDefinition> getRouteDefinitionClass() {
        return ZuulRouteDefinition.class;
    }

    @Override
    protected ZuulTargetRoute buildRouteDefinition(ZuulServiceRouteInfo serviceRouteInfo, ZuulRouteDefinition routeDefinition) {
        Route route = new Route(routeDefinition.getId(), routeDefinition.getPath(), RouteUtil.getZuulLocation(routeDefinition.getUri()), null, RETRYABLE, null);
        return new ZuulTargetRoute(serviceRouteInfo, routeDefinition, route);
    }
}
