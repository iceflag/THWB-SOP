package com.zyd.sop.servercommon.bean;

import com.zyd.sop.servercommon.exception.ZookeeperOperationException;
import com.zyd.sop.servercommon.exception.ZookeeperPathNotExistException;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.springframework.core.env.Environment;

import java.io.Closeable;
import java.io.IOException;


/**
 * @author tanghc
 */
@Slf4j
@Getter
public class ZookeeperTool implements Closeable {

    private CuratorFramework client;
    private Environment environment;

    public ZookeeperTool(Environment environment) {
        this.environment = environment;
        initZookeeperClient(environment);
    }

    public void initZookeeperClient(Environment environment) {
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

        this.client = client;
    }

    /**
     * 获取节点内容
     *
     * @param path
     * @return 返回节点内容
     * @throws ZookeeperPathNotExistException 节点不存在，抛出异常
     */
    public String getData(String path) throws ZookeeperPathNotExistException {
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

    public boolean isPathExist(String path) {
        try {
            return client.checkExists().forPath(path) != null;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * 创建path，如果path存在不报错，静默返回path名称
     *
     * @param path
     * @param data
     * @return
     * @throws Exception
     */
    public String createPath(String path, String data) throws Exception {
        if (isPathExist(path)) {
            return path;
        }
        return getClient().create()
                // 如果指定节点的父节点不存在，则Curator将会自动级联创建父节点
                .creatingParentContainersIfNeeded()
                .forPath(path, data.getBytes());
    }

    /**
     * 新建或保存节点
     *
     * @param path
     * @param data
     * @return 返回path
     * @throws Exception
     */
    public String createOrUpdateData(String path, String data) throws Exception {
        return getClient().create()
                // 如果节点存在则Curator将会使用给出的数据设置这个节点的值
                .orSetData()
                // 如果指定节点的父节点不存在，则Curator将会自动级联创建父节点
                .creatingParentContainersIfNeeded()
                .forPath(path, data.getBytes());
    }

    @Override
    public void close() throws IOException {
        if (this.client != null) {
            this.client.close();
        }
    }
}
