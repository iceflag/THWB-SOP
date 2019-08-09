package com.zyd.sop.websiteserver.bean;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.TreeCache;
import org.apache.curator.framework.recipes.cache.TreeCacheListener;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

/**
 * @author tanghc
 */
@Slf4j
public class ZookeeperContext {

    private static CuratorFramework client;

    public static void setEnvironment(Environment environment) {
        Assert.notNull(environment, "environment不能为null");
        initZookeeperClient(environment);
    }

    public synchronized static void initZookeeperClient(Environment environment) {
        if (client != null) {
            return;
        }
        String zookeeperServerAddr = environment.getProperty("spring.cloud.zookeeper.connect-string");
        if (StringUtils.isBlank(zookeeperServerAddr)) {
            throw new RuntimeException("未指定spring.cloud.zookeeper.connect-string参数");
        }
        String baseSleepTimeMs = environment.getProperty("spring.cloud.zookeeper.baseSleepTimeMs");
        String maxRetries = environment.getProperty("spring.cloud.zookeeper.maxRetries");
        log.info("初始化zookeeper客户端，zookeeperServerAddr:{}, baseSleepTimeMs:{}, maxRetries:{}",
                zookeeperServerAddr, baseSleepTimeMs, maxRetries);
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperServerAddr)
                .retryPolicy(new ExponentialBackoffRetry(NumberUtils.toInt(baseSleepTimeMs, 3000), NumberUtils.toInt(maxRetries, 3)))
                .build();

        client.start();

        setClient(client);
    }

    public static String getRouteRootPath() {
        return WebsiteConstants.SOP_SERVICE_ROUTE_PATH;
    }

    public static CuratorFramework getClient() {
        return client;
    }

    public static void setClient(CuratorFramework client) {
        ZookeeperContext.client = client;
    }

    /**
     * 监听子节点，可以自定义层级
     * @param parentPath 父节点路径
     * @param maxDepth 层级，从1开始。比如当前监听节点/t1，目录最深为/t1/t2/t3/t4,则maxDepth=3,说明下面3级子目录全
     * @param listener
     * @throws Exception
     */
    public static void listenChildren(String parentPath, int maxDepth, TreeCacheListener listener) throws Exception {
        final TreeCache treeCache = TreeCache
                .newBuilder(client, parentPath)
                .setCacheData(true)
                .setMaxDepth(maxDepth)
                .build();

        treeCache.getListenable().addListener(listener);
        //没有开启模式作为入参的方法
        treeCache.start();
    }

}
