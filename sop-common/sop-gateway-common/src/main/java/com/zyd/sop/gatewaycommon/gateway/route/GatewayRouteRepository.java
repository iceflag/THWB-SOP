package com.zyd.sop.gatewaycommon.gateway.route;

import com.zyd.sop.gatewaycommon.bean.TargetRoute;
import com.zyd.sop.gatewaycommon.manager.RouteRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.event.PredicateArgsEvent;
import org.springframework.cloud.gateway.event.RefreshRoutesEvent;
import org.springframework.cloud.gateway.route.RouteDefinition;
import org.springframework.cloud.gateway.route.RouteDefinitionRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.ApplicationEventPublisherAware;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static java.util.Collections.synchronizedMap;

/**
 * 路由存储管理，负责动态更新路由
 *
 * @author tanghc
 */
@Slf4j
public class GatewayRouteRepository implements ApplicationEventPublisherAware,
        RouteDefinitionRepository,
        RouteRepository<GatewayTargetRoute> {

    private final Map<String, GatewayTargetRoute> routes = synchronizedMap(new LinkedHashMap<>());

    private ApplicationEventPublisher publisher;

    @Override
    public Flux<RouteDefinition> getRouteDefinitions() {
        List<RouteDefinition> list = routes.values().parallelStream()
                .map(TargetRoute::getTargetRouteDefinition)
                .filter(routeDefinition -> !routeDefinition.getId().contains("_first.route_"))
                .collect(Collectors.toList());
        return Flux.fromIterable(list);
    }

    @Override
    public Mono<Void> save(Mono<RouteDefinition> route) {
        return null;
    }

    @Override
    public Mono<Void> delete(Mono<String> routeId) {
        return null;
    }

    /**
     * 根据ID获取路由
     */
    @Override
    public GatewayTargetRoute get(String id) {
        if (id == null) {
            return null;
        }
        return routes.get(id);
    }

    @Override
    public Collection<GatewayTargetRoute> getAll() {
        return routes.values();
    }

    /**
     * 增加路由
     */
    @Override
    public String add(GatewayTargetRoute targetRoute) {
        GatewayRouteDefinition baseRouteDefinition = targetRoute.getRouteDefinition();
        routes.put(baseRouteDefinition.getId(), targetRoute);
        this.publisher.publishEvent(new RefreshRoutesEvent(this));
        return "success";
    }

    @Override
    public void update(GatewayTargetRoute targetRoute) {
        GatewayRouteDefinition baseRouteDefinition = targetRoute.getRouteDefinition();
        routes.put(baseRouteDefinition.getId(), targetRoute);
    }

    /**
     * 删除路由
     */
    @Override
    public void delete(String id) {
        routes.remove(id);
        this.publisher.publishEvent(new PredicateArgsEvent(this, id, Collections.emptyMap()));
    }

    @Override
    public void deleteAll(String serviceId) {
        List<String> idList = this.routes.values().stream()
                .map(zuulTargetRoute -> zuulTargetRoute.getRouteDefinition().getId())
                .collect(Collectors.toList());

        for (String id : idList) {
            this.delete(id);
        }
    }

    @Override
    public void setApplicationEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.publisher = applicationEventPublisher;
    }

}
