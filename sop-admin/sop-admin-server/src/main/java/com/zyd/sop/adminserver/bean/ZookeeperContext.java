package com.zyd.sop.adminserver.bean;

import com.zyd.sop.adminserver.common.ZookeeperOperationException;
import com.zyd.sop.adminserver.common.ZookeeperPathExistException;
import com.zyd.sop.adminserver.common.ZookeeperPathNotExistException;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.NodeCache;
import org.apache.curator.framework.recipes.cache.NodeCacheListener;
import org.apache.curator.framework.recipes.cache.PathChildrenCache;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.springframework.core.env.Environment;
import org.springframework.util.Assert;

import java.io.Closeable;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Executors;

/**
 * @author tanghc
 */
@Slf4j
public class ZookeeperContext {

    private static CuratorFramework client;

    private static Environment environment;

    public static void setEnvironment(Environment environment) {
        Assert.notNull(environment, "environment不能为null");
        ZookeeperContext.environment = environment;
        initZookeeperClient();
    }

    public synchronized static void initZookeeperClient() {
        if (client != null) {
            return;
        }
        setClient(createClient());
    }

    public static CuratorFramework createClient() {
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
        return client;
    }

    public static String getSopRouteRootPath() {
        return SopAdminConstants.SOP_SERVICE_ROUTE_PATH;
    }

    public static String buildServiceIdPath(String serviceId) {
        if (StringUtils.isBlank(serviceId)) {
            throw new NullPointerException("serviceId不能为空");
        }
        return getSopRouteRootPath() + "/" + serviceId;
    }

    public static String buildRoutePath(String serviceId, String routeId) {
        if (StringUtils.isBlank(serviceId)) {
            throw new NullPointerException("serviceId不能为空");
        }
        if (StringUtils.isBlank(routeId)) {
            throw new NullPointerException("routeId不能为空");
        }
        String serviceIdPath = getSopRouteRootPath() + "/" + serviceId;
        return serviceIdPath + "/" + routeId;
    }

    public static String getIsvInfoChannelPath() {
        return SopAdminConstants.SOP_MSG_CHANNEL_PATH + "/isvinfo";
    }

    public static String getIsvRoutePermissionChannelPath() {
        return SopAdminConstants.SOP_MSG_CHANNEL_PATH + "/isv-route-permission";
    }

    public static String getRouteConfigChannelPath() {
        return SopAdminConstants.SOP_MSG_CHANNEL_PATH + "/route-conf";
    }

    public static String getLimitConfigChannelPath() {
        return SopAdminConstants.SOP_MSG_CHANNEL_PATH + "/limit-conf";
    }

    public static String getIpBlacklistChannelPath() {
        return SopAdminConstants.SOP_MSG_CHANNEL_PATH + "/ipblacklist-conf";
    }

    public static CuratorFramework getClient() {
        return client;
    }

    public static void setClient(CuratorFramework client) {
        ZookeeperContext.client = client;
    }

    public static boolean isPathExist(String path) {
        try {
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 对已存在的path赋值。如果path不存在抛异常
     *
     * @param path 已存在的
     * @param data 数据
     * @return
     * @throws ZookeeperPathNotExistException
     */
    public static Stat updatePathData(String path, String data) throws ZookeeperPathNotExistException {
        if (!isPathExist(path)) {
            throw new ZookeeperPathNotExistException("path " + path + " 不存在");
        }
        try {
            return getClient().setData().forPath(path, data.getBytes());
        } catch (Exception e) {
            throw new ZookeeperOperationException("updatePathData error, path=" + path, e);
        }
    }

    /**
     * 创建新的path，并赋值。如果path已存在抛异常
     *
     * @param path 待创建的path
     * @param data 值
     * @throws ZookeeperPathExistException
     */
    public static String addPath(String path, String data) throws ZookeeperPathExistException {
        if (isPathExist(path)) {
            throw new ZookeeperPathExistException("path " + path + " 已存在");
        }
        try {
            return addPath(path, CreateMode.PERSISTENT, data);
        } catch (Exception e) {
            throw new ZookeeperOperationException("addPath error path=" + path, e);
        }
    }

    /**
     * 添加节点
     *
     * @param path       待创建的path
     * @param createMode 节点类型
     * @param data       节点数据
     * @return
     */
    public static String addPath(String path, CreateMode createMode, String data) {
        try {
            return getClient().create()
                    // 如果指定节点的父节点不存在，则Curator将会自动级联创建父节点
                    .creatingParentContainersIfNeeded()
                    .withMode(createMode)
                    .forPath(path, data.getBytes());
        } catch (Exception e) {
            throw new ZookeeperOperationException("addPath error path=" + path, e);
        }
    }

    /**
     * 删除节点及子节点
     *
     * @param path
     */
    public static void deletePathDeep(String path) {
        try {
            getClient().delete()
                    .deletingChildrenIfNeeded()
                    .forPath(path);
        } catch (Exception e) {
        }
    }


    /**
     * 创建新的path，并赋值。如果path已存在则不创建
     *
     * @param path 待创建的path
     * @param data 值
     */
    public static String addPathQuietly(String path, String data) {
        if (isPathExist(path)) {
            return path;
        }
        try {
            return addPath(path, data);
        } catch (Exception e) {
            throw new ZookeeperOperationException("addPathQuietly error path=" + path, e);
        }
    }

    /**
     * 新建或保存节点
     *
     * @param path
     * @param data
     * @return
     */
    public static String createOrUpdateData(String path, String data) {
        try {
            return getClient().create()
                    // 如果节点存在则Curator将会使用给出的数据设置这个节点的值
                    .orSetData()
                    // 如果指定节点的父节点不存在，则Curator将会自动级联创建父节点
                    .creatingParentContainersIfNeeded()
                    .forPath(path, data.getBytes());
        } catch (Exception e) {
            throw new ZookeeperOperationException("createOrUpdateData error path=" + path, e);
        }
    }

    /**
     * 获取节点内容
     *
     * @param path
     * @return
     * @throws ZookeeperPathNotExistException
     */
    public static String getData(String path) throws ZookeeperPathNotExistException {
        if (!isPathExist(path)) {
            throw new ZookeeperPathNotExistException("path 不存在, path=" + path);
        }
        try {
            byte[] data = getClient().getData().forPath(path);
            return new String(data);
        } catch (Exception e) {
            throw new ZookeeperOperationException("getData error path=" + path, e);
        }
    }

    /**
     * 获取子节点数据
     *
     * @param parentPath 父节点
     * @return
     */
    public static List<ChildData> getChildrenData(String parentPath) {
        PathChildrenCache pathChildrenCache = buildPathChildrenCache(parentPath);
        if (pathChildrenCache == null) {
            return Collections.emptyList();
        }
        return pathChildrenCache.getCurrentData();
    }

    public static PathChildrenCache buildPathChildrenCache(String path) {
        if (!isPathExist(path)) {
            return null;
        }
        // PathChildrenCache: 监听数据节点的增删改，可以设置触发的事件
        // 且第三个参数要设置为true，不然ChildData对象中的getData返回null
        PathChildrenCache childrenCache = new PathChildrenCache(client, path, true);
        // 列出子节点数据列表，需要使用BUILD_INITIAL_CACHE同步初始化模式才能获得，异步是获取不到的
        try {
            childrenCache.start(PathChildrenCache.StartMode.BUILD_INITIAL_CACHE);
        } catch (Exception e) {
            throw new ZookeeperOperationException("buildPathChildrenCache error path=" + path, e);
        }
        return childrenCache;
    }

    /**
     * 监听一个节点
     *
     * @param path
     * @param listenCallback 回调
     * @return 返回path
     * @throws Exception
     */
    public static void listenTempPath(String path, ListenCallback listenCallback) throws Exception {
        String initData = "{}";
        CuratorFramework client = createClient();
        client.create()
                // 如果指定节点的父节点不存在，则Curator将会自动级联创建父节点
                .creatingParentContainersIfNeeded()
                .withMode(CreateMode.EPHEMERAL)
                .forPath(path, initData.getBytes());

        final NodeCache cache = new NodeCache(client, path, false);
        cache.getListenable().addListener(new NodeCacheListener() {
            @Override
            public void nodeChanged() throws Exception {
                byte[] nodeData = cache.getCurrentData().getData();
                String data = new String(nodeData);
                if (StringUtils.isNotBlank(data) && !initData.equals(data)) {
                    listenCallback.onError(data);
                    Executors.newSingleThreadExecutor().execute(() -> new ZKClose(cache, client));
                }
            }
        });
        cache.start();
    }

    public interface ListenCallback {
        void onError(String errorMsg);
    }

    static class ZKClose implements Runnable {
        Closeable[] closes;

        public ZKClose(Closeable ...closes) {
            this.closes = closes;
        }

        @Override
        public void run() {
            try {
                Thread.sleep(2000);
                IOUtils.closeQuietly(closes);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

}
