package com.zyd.sop.gateway;

import com.zyd.sop.gatewaycommon.bean.SopConstants;
import junit.framework.TestCase;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;

/**
 * @author tanghc
 */
public class CuratorTest extends TestCase {

    private String zookeeperServerAddr = "localhost:2181";

    /**
     * 递归删除节点，只能在测试环境用。
     *
     * @throws Exception
     */
    public void testDel() throws Exception {
        CuratorFramework client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperServerAddr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        client.start();

        try {
            client.delete().deletingChildrenIfNeeded().forPath(SopConstants.SOP_SERVICE_ROUTE_PATH);
        } catch (Exception e) {
        }
    }
}
