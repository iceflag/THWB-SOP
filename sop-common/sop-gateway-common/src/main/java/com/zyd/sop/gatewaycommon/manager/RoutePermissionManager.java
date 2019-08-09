package com.zyd.sop.gatewaycommon.manager;

import com.zyd.sop.gatewaycommon.bean.IsvRoutePermission;

/**
 * @author tanghc
 */
public interface RoutePermissionManager {
    /**
     * 加载路由授权
     */
    void load();

    /**
     * 更新路由授权信息
     *
     * @param isvRoutePermission 授权信息
     */
    void update(IsvRoutePermission isvRoutePermission);
}
