package com.zyd.sop.registryapi.service;

import com.zyd.sop.registryapi.bean.ServiceInfo;
import com.zyd.sop.registryapi.bean.ServiceInstance;

import java.util.List;

/**
 * @author tanghc
 */
public interface RegistryService {
    String TIMESTAMP_PATTERN = "yyyy-MM-dd HH:mm:ss";

    /**
     * 获取所有服务列表
     *
     * @param pageNo   当前页码
     * @param pageSize 分页大小
     * @return 返回服务列表
     * @throws Exception 获取失败抛出异常
     */
    List<ServiceInfo> listAllService(int pageNo, int pageSize) throws Exception;

    /**
     * 服务上线
     *
     * @param serviceInstance
     * @throws Exception 服务上线失败抛出异常
     */
    void onlineInstance(ServiceInstance serviceInstance) throws Exception;

    /**
     * 服务下线
     *
     * @param serviceInstance
     * @throws Exception 服务下线失败抛出异常
     */
    void offlineInstance(ServiceInstance serviceInstance) throws Exception;


}
