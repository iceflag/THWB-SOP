package com.zyd.sop.registryapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.zyd.sop.registryapi.bean.EurekaApplication;
import com.zyd.sop.registryapi.bean.EurekaApps;
import com.zyd.sop.registryapi.bean.EurekaInstance;
import com.zyd.sop.registryapi.bean.EurekaUri;
import com.zyd.sop.registryapi.bean.ServiceInfo;
import com.zyd.sop.registryapi.bean.ServiceInstance;
import com.zyd.sop.registryapi.service.RegistryService;
import lombok.extern.slf4j.Slf4j;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * eureka接口实现
 * @author tanghc
 */
@Slf4j
public class RegistryServiceEureka implements RegistryService {

    private OkHttpClient client = new OkHttpClient();

    @Value("${registry.eureka-server-addr:}")
    private String eurekaUrl;

    @Override
    public List<ServiceInfo> listAllService(int pageNo, int pageSize) throws Exception {
        if (StringUtils.isBlank(eurekaUrl)) {
            return Collections.emptyList();
        }
        String json = this.requestEurekaServer(EurekaUri.QUERY_APPS);
        EurekaApps eurekaApps = JSON.parseObject(json, EurekaApps.class);

        List<ServiceInfo> serviceInfoList = new ArrayList<>();
        List<EurekaApplication> applicationList = eurekaApps.getApplications().getApplication();
        for (EurekaApplication eurekaApplication : applicationList) {
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceId(eurekaApplication.getName());
            List<EurekaInstance> instanceList = eurekaApplication.getInstance();
            if (!CollectionUtils.isEmpty(instanceList)) {
                serviceInfo.setInstances(new ArrayList<>(instanceList.size()));
                for (EurekaInstance eurekaInstance : instanceList) {
                    ServiceInstance serviceInstance = new ServiceInstance();
                    serviceInstance.setInstanceId(eurekaInstance.getInstanceId());
                    serviceInstance.setServiceId(serviceInfo.getServiceId());
                    serviceInstance.setIp(eurekaInstance.getIpAddr());
                    serviceInstance.setPort(Integer.valueOf(eurekaInstance.fetchPort()));
                    serviceInstance.setStatus(eurekaInstance.getStatus());
                    Date updateTime = new Date(Long.valueOf(eurekaInstance.getLastUpdatedTimestamp()));
                    serviceInstance.setUpdateTime(DateFormatUtils.format(updateTime, TIMESTAMP_PATTERN));
                    serviceInfo.getInstances().add(serviceInstance);
                }
            }
            serviceInfoList.add(serviceInfo);
        }
        return serviceInfoList;
    }



    @Override
    public void onlineInstance(ServiceInstance serviceInstance) throws Exception {
        this.requestEurekaServer(EurekaUri.ONLINE_SERVICE, serviceInstance.getServiceId(), serviceInstance.getInstanceId());
    }

    @Override
    public void offlineInstance(ServiceInstance serviceInstance) throws Exception {
        this.requestEurekaServer(EurekaUri.OFFLINE_SERVICE, serviceInstance.getServiceId(), serviceInstance.getInstanceId());
    }

    private String requestEurekaServer(EurekaUri eurekaUri, String... args) throws IOException {
        Request request = eurekaUri.getRequest(this.eurekaUrl, args);
        Response response = client.newCall(request).execute();
        if (response.isSuccessful()) {
            return response.body().string();
        } else {
            log.error("操作失败，url:{}, msg:{}, code:{}", eurekaUri.getUri(args), response.message(), response.code());
            throw new RuntimeException("操作失败");
        }
    }

}
