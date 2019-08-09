package com.zyd.sop.gateway.manager;

import com.zyd.sop.gatewaycommon.bean.ApiConfig;

/**
 * @author tanghc
 */
public class ManagerInitializer {

    public ManagerInitializer() {
        ApiConfig apiConfig = ApiConfig.getInstance();
        apiConfig.setIsvManager(new DbIsvManager());
        apiConfig.setIsvRoutePermissionManager(new DbIsvRoutePermissionManager());
        apiConfig.setRouteConfigManager(new DbRouteConfigManager());
        apiConfig.setLimitConfigManager(new DbLimitConfigManager());
        apiConfig.setIpBlacklistManager(new DbIPBlacklistManager());
    }
}
