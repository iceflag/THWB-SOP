package com.zyd.sop.adminserver.service;

import com.alibaba.fastjson.JSON;
import com.zyd.sop.adminserver.bean.ChannelMsg;
import com.zyd.sop.adminserver.bean.ConfigLimitDto;
import com.zyd.sop.adminserver.bean.RouteConfigDto;
import com.zyd.sop.adminserver.bean.ZookeeperContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

/**
 * @author tanghc
 */
@Service
@Slf4j
public class RouteConfigService {

    /**
     * 发送路由配置消息
     * @param routeConfigDto
     * @throws Exception
     */
    public void sendRouteConfigMsg(RouteConfigDto routeConfigDto) {
        String configData = JSON.toJSONString(routeConfigDto);
        ChannelMsg channelMsg = new ChannelMsg("update", configData);
        String jsonData = JSON.toJSONString(channelMsg);
        String path = ZookeeperContext.getRouteConfigChannelPath();
        log.info("消息推送--路由配置(update), path:{}, data:{}", path, jsonData);
        ZookeeperContext.createOrUpdateData(path, jsonData);
    }

    /**
     * 推送路由配置
     * @param routeConfigDto
     * @throws Exception
     */
    public void sendLimitConfigMsg(ConfigLimitDto routeConfigDto) throws Exception {
        String configData = JSON.toJSONString(routeConfigDto);
        ChannelMsg channelMsg = new ChannelMsg("update", configData);
        String jsonData = JSON.toJSONString(channelMsg);
        String path = ZookeeperContext.getLimitConfigChannelPath();
        log.info("消息推送--限流配置(update), path:{}, data:{}", path, jsonData);
        ZookeeperContext.createOrUpdateData(path, jsonData);
    }
}
