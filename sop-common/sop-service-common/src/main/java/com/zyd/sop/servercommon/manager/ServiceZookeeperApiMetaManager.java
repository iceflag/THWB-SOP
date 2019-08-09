package com.zyd.sop.servercommon.manager;

import com.alibaba.fastjson.JSON;
import com.zyd.sop.servercommon.bean.ServiceApiInfo;
import com.zyd.sop.servercommon.bean.ZookeeperTool;
import com.zyd.sop.servercommon.exception.ZookeeperPathNotExistException;
import com.zyd.sop.servercommon.route.GatewayRouteDefinition;
import com.zyd.sop.servercommon.route.ServiceRouteInfo;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.core.env.Environment;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * 上传路由到zookeeper
 *
 * @author tanghc
 */
@Slf4j
@Getter
@Setter
public class ServiceZookeeperApiMetaManager implements ApiMetaManager {

    /** 网关对应的LoadBalance协议 */
    private static final String PROTOCOL_LOAD_BALANCE = "lb://";

    private static final String PATH_SPLIT = "/";

    private Environment environment;

    private ZookeeperTool zookeeperTool;

    private String serviceId;

    public ServiceZookeeperApiMetaManager(Environment environment) {
        this.environment = environment;
        serviceId = environment.getProperty("spring.application.name");
        if (StringUtils.isEmpty(serviceId)) {
            throw new IllegalArgumentException("请在application.properties中指定spring.application.name属性");
        }
        this.zookeeperTool = new ZookeeperTool(environment);

        this.uploadServiceId(environment);
    }

    /**
     * 上传serviceId目录
     *
     * @param environment environment
     */
    protected void uploadServiceId(Environment environment) {
        try {
            this.checkZookeeperNode(serviceId, "serviceId（" + serviceId + "）不能有斜杠字符'/'");
            ServiceRouteInfo serviceRouteInfo = this.buildServiceRouteInfo();
            // 保存路径
            String savePath = serviceRouteInfo.getZookeeperPath();
            String nodeData = JSON.toJSONString(serviceRouteInfo);
            log.info("serviceId:{}, zookeeper保存路径:{}", serviceId, savePath);
            this.zookeeperTool.createPath(savePath, nodeData);
        } catch (Exception e) {
            throw new IllegalStateException("zookeeper操作失败");
        }
    }

    @Override
    public void uploadApi(ServiceApiInfo serviceApiInfo) {
        try {
            ServiceRouteInfo serviceRouteInfo = this.buildServiceGatewayInfo(serviceApiInfo);
            this.uploadServiceRouteInfoToZookeeper(serviceRouteInfo);
        } finally {
            IOUtils.closeQuietly(zookeeperTool);
        }
    }

    /**
     * 构建接口信息，符合spring cloud gateway的格式
     *
     * @param serviceApiInfo 服务接口信息
     * @return 返回服务路由信息
     */
    protected ServiceRouteInfo buildServiceGatewayInfo(ServiceApiInfo serviceApiInfo) {
        List<ServiceApiInfo.ApiMeta> apis = serviceApiInfo.getApis();
        List<GatewayRouteDefinition> routeDefinitionList = new ArrayList<>(apis.size());
        for (ServiceApiInfo.ApiMeta apiMeta : apis) {
            GatewayRouteDefinition gatewayRouteDefinition = this.buildGatewayRouteDefinition(serviceApiInfo, apiMeta);
            routeDefinitionList.add(gatewayRouteDefinition);
        }
        ServiceRouteInfo serviceRouteInfo = this.buildServiceRouteInfo();
        serviceRouteInfo.setRouteDefinitionList(routeDefinitionList);
        String md5 = buildMd5(routeDefinitionList);
        serviceRouteInfo.setMd5(md5);
        return serviceRouteInfo;
    }

    /**
     * 构建路由id MD5
     *
     * @param routeDefinitionList 路由列表
     * @return 返回MD5
     */
    protected String buildMd5(List<GatewayRouteDefinition> routeDefinitionList) {
        List<String> routeIdList = routeDefinitionList.stream()
                .map(JSON::toJSONString)
                .sorted()
                .collect(Collectors.toList());
        String md5Source = org.apache.commons.lang3.StringUtils.join(routeIdList, "");
        return DigestUtils.md5DigestAsHex(md5Source.getBytes(StandardCharsets.UTF_8));
    }

    protected ServiceRouteInfo buildServiceRouteInfo() {
        ServiceRouteInfo serviceRouteInfo = new ServiceRouteInfo();
        serviceRouteInfo.setServiceId(serviceId);
        String description = environment.getProperty("spring.application.description");
        serviceRouteInfo.setDescription(description);
        return serviceRouteInfo;
    }

    protected GatewayRouteDefinition buildGatewayRouteDefinition(ServiceApiInfo serviceApiInfo, ServiceApiInfo.ApiMeta apiMeta) {
        GatewayRouteDefinition gatewayRouteDefinition = new GatewayRouteDefinition();
        // 唯一id规则：接口名 + 版本号
        String routeId = apiMeta.fetchNameVersion();
        this.checkZookeeperNode(routeId, "接口定义（" + routeId + "）不能有斜杠字符'/'");
        BeanUtils.copyProperties(apiMeta, gatewayRouteDefinition);
        gatewayRouteDefinition.setId(routeId);
        gatewayRouteDefinition.setFilters(Collections.emptyList());
        String uri = this.buildUri(serviceApiInfo, apiMeta);
        String path = this.buildServletPath(serviceApiInfo, apiMeta);
        gatewayRouteDefinition.setUri(uri);
        gatewayRouteDefinition.setPath(path);
        return gatewayRouteDefinition;
    }

    protected String buildUri(ServiceApiInfo serviceApiInfo, ServiceApiInfo.ApiMeta apiMeta) {
        return PROTOCOL_LOAD_BALANCE + serviceApiInfo.getServiceId();
    }

    protected String buildServletPath(ServiceApiInfo serviceApiInfo, ServiceApiInfo.ApiMeta apiMeta) {
        String servletPath = apiMeta.getPath();
        if (servletPath == null) {
            servletPath = "";
        }
        if (!servletPath.startsWith(PATH_SPLIT)) {
            servletPath = PATH_SPLIT + servletPath;
        }
        return servletPath;
    }

    /**
     * 上传接口信息到zookeeper
     *
     * @param serviceRouteInfo 路由服务信息
     */
    protected void uploadServiceRouteInfoToZookeeper(ServiceRouteInfo serviceRouteInfo) {
        String savePath = serviceRouteInfo.getZookeeperPath();
        try {
            String existServiceRouteInfoData = zookeeperTool.getData(savePath);
            ServiceRouteInfo serviceRouteInfoExist = JSON.parseObject(existServiceRouteInfoData, ServiceRouteInfo.class);
            String oldMD5 = serviceRouteInfoExist.getMd5();
            String newMD5 = serviceRouteInfo.getMd5();
            if (Objects.equals(oldMD5, newMD5)) {
                log.info("接口没有改变，无需上传路由信息");
                return;
            }
        } catch (ZookeeperPathNotExistException e) {
            log.warn("服务路径不存在，path:{}", savePath);
        }
        try {
            log.info("上传接口信息到zookeeper，path:{}, serviceId：{}, 接口数量：{}",
                    savePath,
                    serviceRouteInfo.getServiceId(),
                    serviceRouteInfo.getRouteDefinitionList().size());

            String parentPath = this.uploadFolder(serviceRouteInfo);
            this.uploadRouteItems(serviceRouteInfo, parentPath);
        } catch (Exception e) {
            log.error("更新接口信息到zookeeper失败, serviceId:{}", serviceRouteInfo.getServiceId(), e);
        }
    }

    /**
     * 上传文件夹内容
     *
     * @param serviceRouteInfo 路由服务信息
     * @return 返回文件夹路径
     */
    protected String uploadFolder(ServiceRouteInfo serviceRouteInfo) throws Exception {
        // 保存路径
        String savePath = serviceRouteInfo.getZookeeperPath();
        String serviceRouteInfoJson = JSON.toJSONString(serviceRouteInfo);
        log.info("上传service目录到zookeeper，路径:{}，内容:{}", savePath, serviceRouteInfoJson);
        this.zookeeperTool.createOrUpdateData(savePath, serviceRouteInfoJson);
        return savePath;
    }

    /**
     * 上传路由信息
     *
     * @param serviceRouteInfo 路由服务
     * @param parentPath       父节点
     * @throws Exception
     */
    protected void uploadRouteItems(ServiceRouteInfo serviceRouteInfo, String parentPath) throws Exception {
        List<GatewayRouteDefinition> routeDefinitionList = serviceRouteInfo.getRouteDefinitionList();
        for (GatewayRouteDefinition routeDefinition : routeDefinitionList) {
            // 父目录/子目录
            String savePath = parentPath + PATH_SPLIT + routeDefinition.getId();
            String routeDefinitionJson = JSON.toJSONString(routeDefinition);
            log.info("上传路由配置到zookeeper，路径:{}，路由数据:{}", savePath, routeDefinitionJson);
            this.zookeeperTool.createOrUpdateData(savePath, routeDefinitionJson);
        }
    }

    private void checkZookeeperNode(String path, String errorMsg) {
        if (path.contains(PATH_SPLIT)) {
            throw new IllegalArgumentException(errorMsg);
        }
    }

}
