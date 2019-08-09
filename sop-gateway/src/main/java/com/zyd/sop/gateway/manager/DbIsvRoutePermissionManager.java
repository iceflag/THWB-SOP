package com.zyd.sop.gateway.manager;

import com.alibaba.fastjson.JSON;
import com.gitee.fastmybatis.core.query.Query;
import com.zyd.sop.gateway.entity.IsvInfo;
import com.zyd.sop.gateway.entity.PermIsvRole;
import com.zyd.sop.gateway.entity.PermRolePermission;
import com.zyd.sop.gateway.mapper.IsvInfoMapper;
import com.zyd.sop.gateway.mapper.PermIsvRoleMapper;
import com.zyd.sop.gateway.mapper.PermRolePermissionMapper;
import com.zyd.sop.gatewaycommon.bean.ChannelMsg;
import com.zyd.sop.gatewaycommon.bean.IsvRoutePermission;
import com.zyd.sop.gatewaycommon.manager.DefaultIsvRoutePermissionManager;
import com.zyd.sop.gatewaycommon.manager.ZookeeperContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.mapping;
import static java.util.stream.Collectors.toList;

/**
 * 从数据库中读取路由权限信息
 *
 * @author tanghc
 */
@Slf4j
public class DbIsvRoutePermissionManager extends DefaultIsvRoutePermissionManager {

    @Autowired
    Environment environment;

    @Autowired
    PermIsvRoleMapper permIsvRoleMapper;

    @Autowired
    PermRolePermissionMapper permRolePermissionMapper;

    @Autowired
    IsvInfoMapper isvInfoMapper;

    @Override
    public void load() {
        // key: appKey, value: roleCodeList
        Map<String, List<String>> appKeyRoleCodeMap = this.getIsvRoleCode();

        for (Map.Entry<String, List<String>> entry : appKeyRoleCodeMap.entrySet()) {
            this.loadIsvRoutePermission(entry.getKey(), entry.getValue());
        }
    }

    public void loadIsvRoutePermission(String appKey, List<String> roleCodeList) {
        Collections.sort(roleCodeList);
        List<String> routeIdList = this.getRouteIdList(roleCodeList);
        String roleCodeListMd5 = DigestUtils.md5Hex(JSON.toJSONString(routeIdList));
        IsvRoutePermission isvRoutePermission = new IsvRoutePermission();
        isvRoutePermission.setAppKey(appKey);
        isvRoutePermission.setRouteIdList(routeIdList);
        isvRoutePermission.setRouteIdListMd5(roleCodeListMd5);
        this.update(isvRoutePermission);
    }

    // 获取ISV对应的角色
    public Map<String, List<String>> getIsvRoleCode() {
        Query query = new Query();
        List<PermIsvRole> permIsvRoles = permIsvRoleMapper.list(query);
        Map<String, List<String>> appKeyRoleCodeMap = permIsvRoles.stream()
                .map(permIsvRole -> {
                    IsvInfo isvInfo = isvInfoMapper.getById(permIsvRole.getIsvId());
                    if (isvInfo == null) {
                        return null;
                    }
                    IsvRole isvRole = new IsvRole();
                    isvRole.appKey = isvInfo.getAppKey();
                    isvRole.roleCode = permIsvRole.getRoleCode();
                    return isvRole;
                })
                .filter(Objects::nonNull)
                .collect(Collectors.groupingBy(IsvRole::getAppKey,
                        mapping(IsvRole::getRoleCode, toList()))
                );
        return appKeyRoleCodeMap;
    }

    /**
     * 获取角色对应的路由
     *
     * @param roleCodeList
     * @return
     */
    public List<String> getRouteIdList(List<String> roleCodeList) {
        if (CollectionUtils.isEmpty(roleCodeList)) {
            return Collections.emptyList();
        }
        Query query = new Query();
        query.in("role_code", roleCodeList);
        List<PermRolePermission> rolePermissionList = permRolePermissionMapper.list(query);
        return rolePermissionList.stream()
                .map(PermRolePermission::getRouteId)
                .collect(Collectors.toList());
    }

    @Data
    static class IsvRole {
        private String appKey;
        private String roleCode;
    }


    @PostConstruct
    protected void after() throws Exception {
        ZookeeperContext.setEnvironment(environment);
        String isvChannelPath = ZookeeperContext.getIsvRoutePermissionChannelPath();
        ZookeeperContext.listenPath(isvChannelPath, nodeCache -> {
            String nodeData = new String(nodeCache.getCurrentData().getData());
            ChannelMsg channelMsg = JSON.parseObject(nodeData, ChannelMsg.class);
            final IsvRoutePermission isvRoutePermission = JSON.parseObject(channelMsg.getData(), IsvRoutePermission.class);
            switch (channelMsg.getOperation()) {
                case "reload":
                    log.info("重新加载路由权限信息，isvRoutePermission:{}", isvRoutePermission);
                    String listenPath = isvRoutePermission.getListenPath();
                    String code = "";
                    try {
                        load();
                    } catch (Exception e) {
                        log.error("重新加载路由权限失败, channelMsg:{}", channelMsg, e);
                        code = e.getMessage();
                    }
                    try {
                        ZookeeperContext.updatePath(listenPath, code);
                    } catch (Exception e1) {
                        log.error("重新加载路由权限信息, zookeeper操作失败， path: {}", listenPath, e1);
                    }
                    break;
                case "update":
                    log.info("更新ISV路由权限信息，isvRoutePermission:{}", isvRoutePermission);
                    update(isvRoutePermission);
                    break;
                case "remove":
                    log.info("删除ISV路由权限信息，isvRoutePermission:{}", isvRoutePermission);
                    remove(isvRoutePermission.getAppKey());
                    break;
            }
        });
    }
}
