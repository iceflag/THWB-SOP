package com.zyd.sop.adminserver.service;

import com.alibaba.fastjson.JSON;
import com.gitee.fastmybatis.core.query.Query;
import com.zyd.sop.adminserver.api.service.param.RoutePermissionParam;
import com.zyd.sop.adminserver.bean.ChannelMsg;
import com.zyd.sop.adminserver.bean.IsvRoutePermission;
import com.zyd.sop.adminserver.bean.SopAdminConstants;
import com.zyd.sop.adminserver.bean.ZookeeperContext;
import com.zyd.sop.adminserver.entity.PermIsvRole;
import com.zyd.sop.adminserver.entity.PermRolePermission;
import com.zyd.sop.adminserver.mapper.IsvInfoMapper;
import com.zyd.sop.adminserver.mapper.PermIsvRoleMapper;
import com.zyd.sop.adminserver.mapper.PermRolePermissionMapper;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 路由权限业务类
 *
 * @author tanghc
 */
@Service
@Slf4j
public class RoutePermissionService {

    @Autowired
    IsvInfoMapper isvInfoMapper;

    @Autowired
    PermIsvRoleMapper permIsvRoleMapper;

    @Autowired
    PermRolePermissionMapper permRolePermissionMapper;

    /**
     * 获取客户端角色码列表
     *
     * @param isvId
     * @return
     */
    public List<String> listClientRoleCode(long isvId) {
        List<PermIsvRole> list = permIsvRoleMapper.listByColumn("isv_id", isvId);
        return list.stream()
                .map(PermIsvRole::getRoleCode)
                .collect(Collectors.toList());
    }

    /**
     * 推送isv路由权限到zookeeper
     *
     * @param appKey
     * @param roleCodeList
     */
    public void sendIsvRolePermissionToZookeeper(String appKey, List<String> roleCodeList) throws Exception {
        Collections.sort(roleCodeList);
        List<String> routeIdList = this.getRouteIdList(roleCodeList);
        String roleCodeListMd5 = DigestUtils.md5Hex(JSON.toJSONString(routeIdList));
        IsvRoutePermission isvRoutePermission = new IsvRoutePermission();
        isvRoutePermission.setAppKey(appKey);
        isvRoutePermission.setRouteIdList(routeIdList);
        isvRoutePermission.setRouteIdListMd5(roleCodeListMd5);
        ChannelMsg channelMsg = new ChannelMsg("update", isvRoutePermission);
        String jsonData = JSON.toJSONString(channelMsg);
        String path = ZookeeperContext.getIsvRoutePermissionChannelPath();
        log.info("消息推送--路由权限(update), path:{}, data:{}", path, jsonData);
        ZookeeperContext.createOrUpdateData(path, jsonData);
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

    /**
     * 推送所有路由权限到zookeeper
     */
    public void sendRoutePermissionReloadMsg(RoutePermissionParam oldRoutePermission) throws Exception {
        String listenPath = SopAdminConstants.RELOAD_ROUTE_PERMISSION_PATH + "/" + System.currentTimeMillis();
        ZookeeperContext.listenTempPath(listenPath, errorMsg -> {
            log.error("推送所有路由权限到zookeeper失败，进行回滚，errorMsg: {}，oldRoutePermission：{}", errorMsg, JSON.toJSONString(oldRoutePermission));
            // 回滚
            updateRoutePermission(oldRoutePermission);
        });
        IsvRoutePermission isvRoutePermission = new IsvRoutePermission();
        isvRoutePermission.setListenPath(listenPath);
        ChannelMsg channelMsg = new ChannelMsg("reload", isvRoutePermission);
        String jsonData = JSON.toJSONString(channelMsg);
        String path = ZookeeperContext.getIsvRoutePermissionChannelPath();
        log.info("消息推送--路由权限(reload), path:{}, data:{}", path, jsonData);
        ZookeeperContext.createOrUpdateData(path, jsonData);
    }

    /**
     * 更新路由权限
     *
     * @param param
     */
    public synchronized void updateRoutePermission(RoutePermissionParam param) {
        String routeId = param.getRouteId();
        // 删除所有数据
        Query delQuery = new Query();
        delQuery.eq("route_id", routeId);
        permRolePermissionMapper.deleteByQuery(delQuery);

        List<String> roleCodes = param.getRoleCode();
        if (org.apache.commons.collections.CollectionUtils.isNotEmpty(roleCodes)) {
            List<PermRolePermission> tobeSave = new ArrayList<>(roleCodes.size());
            for (String roleCode : roleCodes) {
                PermRolePermission permRolePermission = new PermRolePermission();
                permRolePermission.setRoleCode(roleCode);
                permRolePermission.setRouteId(routeId);
                tobeSave.add(permRolePermission);
            }
            // 批量添加
            permRolePermissionMapper.saveBatch(tobeSave);
        }
    }
}
