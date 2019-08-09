package com.zyd.sop.registryapi.service.impl;

import com.alibaba.nacos.api.exception.NacosException;
import com.alibaba.nacos.api.naming.NamingFactory;
import com.alibaba.nacos.api.naming.NamingService;
import com.alibaba.nacos.api.naming.pojo.Instance;
import com.alibaba.nacos.api.naming.pojo.ListView;
import com.zyd.sop.registryapi.bean.ServiceInfo;
import com.zyd.sop.registryapi.bean.ServiceInstance;
import com.zyd.sop.registryapi.service.RegistryService;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * nacos接口实现
 * @author tanghc
 */
public class RegistryServiceNacos implements RegistryService {

    OkHttpClient client = new OkHttpClient();

    @Value("${registry.nacos-server-addr:}")
    private String nacosAddr;

    private NamingService namingService;

    @PostConstruct
    public void after() throws NacosException {
        if (StringUtils.isNotBlank(nacosAddr)) {
            namingService = NamingFactory.createNamingService(nacosAddr);
        }
    }

    @Override
    public List<ServiceInfo> listAllService(int pageNo, int pageSize) throws Exception {
        ListView<String> servicesOfServer = namingService.getServicesOfServer(pageNo, pageSize);
        List<String> serverList = servicesOfServer.getData();
        List<ServiceInfo> serviceInfoList = new ArrayList<>();
        for (String serviceName : serverList) {
            ServiceInfo serviceInfo = new ServiceInfo();
            serviceInfo.setServiceId(serviceName);
            List<Instance> instanceList = namingService.getAllInstances(serviceName);
            if (!CollectionUtils.isEmpty(instanceList)) {
                serviceInfo.setInstances(new ArrayList<>(instanceList.size()));
                for (Instance instance : instanceList) {
                    ServiceInstance serviceInstance = new ServiceInstance();
                    serviceInstance.setInstanceId(instance.getInstanceId());
                    serviceInstance.setServiceId(serviceInfo.getServiceId());
                    serviceInstance.setIp(instance.getIp());
                    serviceInstance.setPort(instance.getPort());
                    boolean isOnline = instance.getWeight() > 0;
                    String status = isOnline ? "UP" : "OUT_OF_SERVICE";
                    serviceInstance.setStatus(status);
                    serviceInfo.getInstances().add(serviceInstance);
                }
            }
            serviceInfoList.add(serviceInfo);
        }
        return serviceInfoList;
    }

    @Override
    public void onlineInstance(ServiceInstance serviceInstance) throws Exception {
        Map<String, String> params = new HashMap<>(8);
        // 上线，把权重设置成1
        params.put("weight", "1");
        this.updateInstance(serviceInstance, params);
    }

    @Override
    public void offlineInstance(ServiceInstance serviceInstance) throws Exception {
        Map<String, String> params = new HashMap<>(8);
        // 下线，把权重设置成0
        params.put("weight", "0");
        this.updateInstance(serviceInstance, params);
    }

    private Response updateInstance(ServiceInstance serviceInstance, Map<String, String> params) throws IOException {
        FormBody.Builder builder = new FormBody.Builder();
        for (Map.Entry<String, String> entry : params.entrySet()) {
            builder.add(entry.getKey(), String.valueOf(entry.getValue()));
        }
        builder.add("serviceName", serviceInstance.getServiceId())
                .add("ip", serviceInstance.getIp())
                .add("port", String.valueOf(serviceInstance.getPort()));
        FormBody formBody = builder.build();
        final Request request = new Request.Builder()
                .url("http://" + nacosAddr + "/nacos/v1/ns/instance")
                .put(formBody)
                .build();

        return client.newCall(request).execute();
    }
}
