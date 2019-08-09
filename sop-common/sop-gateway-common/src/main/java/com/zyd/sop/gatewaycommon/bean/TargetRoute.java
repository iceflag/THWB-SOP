package com.zyd.sop.gatewaycommon.bean;

/**
 * @author tanghc
 */
public interface TargetRoute<R extends BaseServiceRouteInfo, E extends BaseRouteDefinition, T> {

    /**
     * 返回服务信息
     *
     * @return 返回服务信息
     */
    R getServiceRouteInfo();

    /**
     * 返回微服务路由对象
     *
     * @return 返回微服务路由对象
     */
    E getRouteDefinition();

    /**
     * 返回网关路由对象
     *
     * @return 返回网关路由对象
     */
    T getTargetRouteDefinition();
}
