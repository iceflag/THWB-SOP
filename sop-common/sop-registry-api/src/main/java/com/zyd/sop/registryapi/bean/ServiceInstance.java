package com.zyd.sop.registryapi.bean;

import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class ServiceInstance {
    /**
     * 实例id
     */
    private String instanceId;

    /**
     * 服务名称
     */
    private String serviceId;

    /**
     * ip
     */
    private String ip;

    /**
     * port
     */
    private int port;

    /**
     * 服务状态，UP：已上线，OUT_OF_SERVICE：已下线
     */
    private String status;

    /**
     * 更新时间
     */
    private String updateTime;

}
