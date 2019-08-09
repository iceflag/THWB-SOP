package com.zyd.sop.gateway.manager;

import com.alibaba.fastjson.JSON;
import com.gitee.fastmybatis.core.query.Query;
import com.zyd.sop.gateway.mapper.ConfigLimitMapper;
import com.zyd.sop.gatewaycommon.bean.ChannelMsg;
import com.zyd.sop.gatewaycommon.bean.ConfigLimitDto;
import com.zyd.sop.gatewaycommon.manager.DefaultLimitConfigManager;
import com.zyd.sop.gatewaycommon.manager.ZookeeperContext;
import com.zyd.sop.gatewaycommon.util.MyBeanUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;

/**
 * 限流配置管理
 * @author tanghc
 */
@Slf4j
public class DbLimitConfigManager extends DefaultLimitConfigManager {

    @Autowired
    ConfigLimitMapper configLimitMapper;

    @Autowired
    Environment environment;

    @Override
    public void load() {
        Query query = new Query();
        configLimitMapper.list(query)
                .stream()
                .forEach(configLimit -> putVal(configLimit));

    }

    protected void putVal(Object object) {
        ConfigLimitDto configLimitDto = new ConfigLimitDto();
        MyBeanUtil.copyPropertiesIgnoreNull(object, configLimitDto);
        this.update(configLimitDto);
    }


    @PostConstruct
    protected void after() throws Exception {
        ZookeeperContext.setEnvironment(environment);
        String path = ZookeeperContext.getLimitConfigChannelPath();
        ZookeeperContext.listenPath(path, nodeCache -> {
            String nodeData = new String(nodeCache.getCurrentData().getData());
            ChannelMsg channelMsg = JSON.parseObject(nodeData, ChannelMsg.class);
            final ConfigLimitDto configLimitDto = JSON.parseObject(channelMsg.getData(), ConfigLimitDto.class);
            switch (channelMsg.getOperation()) {
                case "reload":
                    log.info("重新加载限流配置信息，configLimitDto:{}", configLimitDto);
                    load();
                    break;
                case "update":
                    log.info("更新限流配置信息，configLimitDto:{}", configLimitDto);
                    update(configLimitDto);
                    break;
                default:
                    log.error("限流配置信息，错误的消息指令，nodeData：{}", nodeData);
            }
        });
    }

}
