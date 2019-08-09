package com.zyd.sop.websiteserver.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.parser.Feature;
import com.zyd.sop.registryapi.bean.ServiceInfo;
import com.zyd.sop.registryapi.bean.ServiceInstance;
import com.zyd.sop.registryapi.service.RegistryService;
import com.zyd.sop.websiteserver.bean.DocInfo;
import com.zyd.sop.websiteserver.bean.DocItem;
import com.zyd.sop.websiteserver.bean.ZookeeperContext;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.TreeCacheEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.DelayQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@Service
@Slf4j
public class DocManagerImpl implements DocManager {

    // key:title
    Map<String, DocInfo> docDefinitionMap = new HashMap<>();

    // key: name+version
    Map<String, DocItem> docItemMap = new HashMap<>();


    RestTemplate restTemplate = new RestTemplate();

    DocParser swaggerDocParser = new SwaggerDocParser();

    DocParser easyopenDocParser = new EasyopenDocParser();

    ExecutorService executorService = Executors.newSingleThreadExecutor();

    DelayQueue<Msg> queue = new DelayQueue<>();

    private String secret = "b749a2ec000f4f29";

    @Autowired
    private Environment environment;

    @Autowired
    private RegistryService registryService;

    private volatile boolean listenInited;

    @Override
    public void load(String serviceId) {
        try {
            List<ServiceInfo> serviceInfoList = registryService.listAllService(1, 9999);
            log.info("服务列表：{}", serviceInfoList);

            serviceInfoList
                    .stream()
                    // 网关没有文档提供，需要排除
                    .filter(serviceInfo -> !"API-GATEWAY".equalsIgnoreCase(serviceInfo.getServiceId()))
                    .filter(serviceInfo -> !serviceInfo.getInstances().isEmpty())
                    .filter(serviceInfo -> {
                        if (StringUtils.isEmpty(serviceId)) {
                            return true;
                        }
                        return serviceId.equalsIgnoreCase(serviceInfo.getServiceId());
                    })
                    .map(serviceInfo -> serviceInfo.getInstances().get(0))
                    .collect(Collectors.toList())
                    .forEach(this::loadDocInfo);
        } catch (Exception e) {
            log.error("加载失败", e);
        }
    }

    protected void loadDocInfo(ServiceInstance serviceInstance) {
        String query = this.buildQuery();
        String url = "http://" + serviceInstance.getIp() + ":" + serviceInstance.getPort() + "/v2/api-docs" + query;
        try {
            log.info("读取swagger文档，serviceId:{}, url:{}", serviceInstance.getServiceId(), url);
            ResponseEntity<String> entity = restTemplate.getForEntity(url, String.class);
            if (entity.getStatusCode() != HttpStatus.OK) {
                throw new IllegalAccessException("无权访问");
            }
            String docInfoJson = entity.getBody();
            JSONObject docRoot = JSON.parseObject(docInfoJson, Feature.OrderedField, Feature.DisableCircularReferenceDetect);
            DocParser docParser = this.buildDocParser(docRoot);
            DocInfo docInfo = docParser.parseJson(docRoot);
            docDefinitionMap.put(docInfo.getTitle(), docInfo);
        } catch (Exception e) {
            // 这里报错可能是因为有些微服务没有配置swagger文档，导致404访问不到
            // 这里catch跳过即可
            log.warn("读取文档失败, url:{}, msg:{}", url, e.getMessage());
        }
    }

    protected String buildQuery() {
        String time = String.valueOf(System.currentTimeMillis());
        String source = secret + time + secret;
        String sign = DigestUtils.md5DigestAsHex(source.getBytes());
        return "?time=" + time + "&sign=" + sign;
    }

    protected DocParser buildDocParser(JSONObject rootDoc) {
        Object easyopen = rootDoc.get("easyopen");
        if (easyopen != null) {
            return easyopenDocParser;
        } else {
            return swaggerDocParser;
        }
    }

    @Override
    public DocItem get(String method, String version) {
        return docItemMap.get(method + version);
    }

    @Override
    public DocInfo getByTitle(String title) {
        return docDefinitionMap.get(title);
    }

    @Override
    public Collection<DocInfo> listAll() {
        return docDefinitionMap.values();
    }

    @PostConstruct
    protected void after() throws Exception {
        this.listenServiceId();
    }

    /**
     * 监听serviceId更改
     *
     * @throws Exception
     */
    protected void listenServiceId() throws Exception {

        executorService.execute(new Consumer(queue, this));

        ZookeeperContext.setEnvironment(environment);
        String routeRootPath = ZookeeperContext.getRouteRootPath();
        // 如果节点内容有变化则自动更新文档
        ZookeeperContext.listenChildren(routeRootPath, 1, (client, event) -> {
            if (listenInited) {
                long id = System.currentTimeMillis();
                byte[] data = event.getData().getData();
                String serviceInfoJson = new String(data);
                ZKServiceInfo serviceInfo = JSON.parseObject(serviceInfoJson, ZKServiceInfo.class);
                String serviceId = serviceInfo.getServiceId();
                log.info("微服务[{}]推送更新", serviceId);
                Msg msg = new Msg(id, 1000 * 20);
                msg.serviceId = serviceId;
                // 延迟20秒执行
                queue.offer(msg);
            }
            TreeCacheEvent.Type type = event.getType();
            if (type == TreeCacheEvent.Type.INITIALIZED) {
                listenInited = true;
            }
        });
    }

    static class Msg implements Delayed {
        private long id;
        private long delay;
        private String serviceId;

        // 自定义实现比较方法返回 1 0 -1三个参数


        public Msg(long id, long delay) {
            this.id = id;
            this.delay = delay + System.currentTimeMillis();
        }

        @Override
        public int compareTo(Delayed delayed) {
            Msg msg = (Msg) delayed;
            return Long.valueOf(this.id) > Long.valueOf(msg.id) ? 1
                    : (Long.valueOf(this.id) < Long.valueOf(msg.id) ? -1 : 0);
        }

        // 延迟任务是否到时就是按照这个方法判断如果返回的是负数则说明到期否则还没到期
        @Override
        public long getDelay(TimeUnit unit) {
            return unit.convert(this.delay - System.currentTimeMillis(), TimeUnit.MILLISECONDS);
        }
    }

    @Data
    static class ZKServiceInfo {
        /** 服务名称，对应spring.application.name */
        private String serviceId;

        private String description;
    }

    @Slf4j
    static class Consumer implements Runnable {
        private DelayQueue<Msg> queue;
        private DocManager docManager;

        public Consumer(DelayQueue<Msg> queue, DocManager docManager) {
            this.queue = queue;
            this.docManager = docManager;
        }

        @Override
        public void run() {
            while (true) {
                try {
                    Msg msg = queue.take();
                    log.info("延迟队列触发--重新加载文档信息");
                    docManager.load(msg.serviceId);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
