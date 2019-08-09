package com.zyd.sop.gatewaycommon.zuul.route;

import com.zyd.sop.gatewaycommon.manager.RouteRepository;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 本地存放路由内容的地方
 *
 * @author tanghc
 */
public class ZuulRouteRepository implements RouteRepository<ZuulTargetRoute> {
    /**
     * key：nameVersion
     */
    private Map<String, ZuulTargetRoute> nameVersionTargetRouteMap = new ConcurrentHashMap<>(128);

    @Override
    public ZuulTargetRoute get(String id) {
        if (id == null) {
            return null;
        }
        return nameVersionTargetRouteMap.get(id);
    }

    @Override
    public Collection<ZuulTargetRoute> getAll() {
        return nameVersionTargetRouteMap.values();
    }

    @Override
    public String add(ZuulTargetRoute targetRoute) {
        nameVersionTargetRouteMap.put(targetRoute.getRouteDefinition().getId(), targetRoute);
        return targetRoute.getRouteDefinition().getId();
    }

    @Override
    public void update(ZuulTargetRoute targetRoute) {
        nameVersionTargetRouteMap.put(targetRoute.getRouteDefinition().getId(), targetRoute);
    }

    @Override
    public void deleteAll(String serviceId) {
        Collection<ZuulTargetRoute> values = nameVersionTargetRouteMap.values();
        List<String> idList = values.stream()
                .filter(zuulTargetRoute -> zuulTargetRoute.getServiceRouteInfo().getServiceId().equalsIgnoreCase(serviceId))
                .map(zuulTargetRoute -> zuulTargetRoute.getRouteDefinition().getId())
                .collect(Collectors.toList());

        for (String id : idList) {
            this.delete(id);
        }
    }

    @Override
    public void delete(String id) {
        nameVersionTargetRouteMap.remove(id);
    }
}
