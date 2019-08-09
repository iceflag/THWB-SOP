package com.zyd.sop.registryapi.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.Collections;
import java.util.List;

/**
 * @author tanghc
 */
@Getter
@Setter
public class ServiceInfo {
    /** 服务名称 */
    private String serviceId;
    /** 实例列表 */
    private List<ServiceInstance> instances = Collections.emptyList();

    @Override
    public String toString() {
        return "服务名称: " + serviceId + ", 实例数：" + instances.size();
    }
}
