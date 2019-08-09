package com.zyd.sop.gateway.manager;

import com.alibaba.fastjson.JSON;
import com.gitee.fastmybatis.core.query.Query;
import com.zyd.sop.gateway.mapper.ConfigRouteBaseMapper;
import com.zyd.sop.gateway.mapper.ConfigRouteLimitMapper;
import com.zyd.sop.gatewaycommon.bean.BaseRouteDefinition;
import com.zyd.sop.gatewaycommon.bean.ChannelMsg;
import com.zyd.sop.gatewaycommon.bean.RouteConfig;
import com.zyd.sop.gatewaycommon.bean.TargetRoute;
import com.zyd.sop.gatewaycommon.manager.DefaultRouteConfigManager;
import com.zyd.sop.gatewaycommon.manager.RouteRepositoryContext;
import com.zyd.sop.gatewaycommon.manager.ZookeeperContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import java.util.Collection;

/**
 * @author tanghc
 */
@Slf4j
public class DbRouteConfigManager extends DefaultRouteConfigManager {

    @Autowired
    ConfigRouteBaseMapper configRouteBaseMapper;

    @Autowired
    ConfigRouteLimitMapper configRouteLimitMapper;

    @Autowired
    Environment environment;

    @Override
    public void load() {
        loadAllRoute();

        Query query = new Query();
        configRouteBaseMapper.list(query)
                .stream()
                .forEach(configRouteBase -> {
                    String key = configRouteBase.getRouteId();
                    putVal(key, configRouteBase);
                });
    }

    protected void loadAllRoute() {
        Collection<? extends TargetRoute> targetRoutes = RouteRepositoryContext.getRouteRepository().getAll();
        targetRoutes.stream()
                .forEach(targetRoute -> {
                    BaseRouteDefinition routeDefinition = targetRoute.getRouteDefinition();
                    initRouteConfig(routeDefinition);
                });
    }

    protected void initRouteConfig(BaseRouteDefinition routeDefinition) {
        String routeId = routeDefinition.getId();
        RouteConfig routeConfig = newRouteConfig();
        routeConfig.setRouteId(routeId);
        routeConfigMap.put(routeId, routeConfig);
    }

    protected void putVal(String routeId, Object object) {
        this.doUpdate(routeId, object);
    }


    @PostConstruct
    protected void after() throws Exception {
        ZookeeperContext.setEnvironment(environment);
        String path = ZookeeperContext.getRouteConfigChannelPath();
        ZookeeperContext.listenPath(path, nodeCache -> {
            String nodeData = new String(nodeCache.getCurrentData().getData());
            ChannelMsg channelMsg = JSON.parseObject(nodeData, ChannelMsg.class);
            final RouteConfig routeConfig = JSON.parseObject(channelMsg.getData(), RouteConfig.class);
            switch (channelMsg.getOperation()) {
                case "reload":
                    log.info("重新加载路由配置信息，routeConfigDto:{}", routeConfig);
                    load();
                    break;
                case "update":
                    log.info("更新路由配置信息，routeConfigDto:{}", routeConfig);
                    update(routeConfig);
                    break;
                default:
                    log.error("路由配置信息，错误的消息指令，nodeData：{}", nodeData);
            }
        });
    }
}
