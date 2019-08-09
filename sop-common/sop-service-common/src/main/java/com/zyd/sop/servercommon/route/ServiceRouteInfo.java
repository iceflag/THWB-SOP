package com.zyd.sop.servercommon.route;

import com.alibaba.fastjson.annotation.JSONField;
import com.zyd.sop.servercommon.bean.ServiceConstants;
import lombok.Data;

import java.util.Date;
import java.util.List;

/**
 * @author tanghc
 */
@Data
public class ServiceRouteInfo {
    private static final String SOP_SERVICE_ROUTE_PATH = ServiceConstants.SOP_SERVICE_ROUTE_PATH;

    /** 服务名称，对应spring.application.name */
    private String serviceId;

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date createTime = new Date();

    @JSONField(format = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime = new Date();

    private String description;

    /** 路由信息md5，md5(sort(routeIdList)) */
    private String md5;

    @JSONField(serialize = false)
    private List<GatewayRouteDefinition> routeDefinitionList;

    /**
     * 返回zookeeper路径
     * @return 返回zookeeper路径
     */
    @JSONField(serialize = false)
    public String getZookeeperPath() {
        return SOP_SERVICE_ROUTE_PATH + '/' + serviceId;
    }
}
