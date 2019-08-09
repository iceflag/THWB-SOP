package com.zyd.sop.gatewaycommon.bean;

/**
 * @author tanghc
 */
public abstract class AbstractTargetRoute<R extends BaseServiceRouteInfo, E extends BaseRouteDefinition, T> implements TargetRoute<R,E, T> {

    private R serviceRouteInfo;
    private E routeDefinition;
    private T targetRoute;

    public AbstractTargetRoute(R serviceRouteInfo, E routeDefinition, T targetRoute) {
        this.serviceRouteInfo = serviceRouteInfo;
        this.routeDefinition = routeDefinition;
        this.targetRoute = targetRoute;
    }

    @Override
    public R getServiceRouteInfo() {
        return serviceRouteInfo;
    }

    @Override
    public E getRouteDefinition() {
        return routeDefinition;
    }

    @Override
    public T getTargetRouteDefinition() {
        return targetRoute;
    }
}
