package com.zyd.sop.adminserver;

import com.zyd.sop.adminserver.bean.SopAdminConstants;
import junit.framework.TestCase;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.data.Stat;

public class CuratorTest extends TestCase {

    private static String zookeeperServerAddr = "localhost:2181";

    static CuratorFramework client;

    public CuratorTest() {
        client = CuratorFrameworkFactory.builder()
                .connectString(zookeeperServerAddr)
                .retryPolicy(new ExponentialBackoffRetry(1000, 3))
                .build();

        client.start();
    }

    /**
     * 递归删除节点，只能在测试环境用。
     *
     * @throws Exception
     */
    public void testDel() {
        try {
            client.delete()
                    .deletingChildrenIfNeeded()
                    .forPath(SopAdminConstants.RELOAD_ROUTE_PERMISSION_PATH);
        } catch (Exception e) {
        }
    }

    public void testCheck() throws Exception {
        String path = SopAdminConstants.RELOAD_ROUTE_PERMISSION_PATH + "/1562231019332";
        Stat stat = client.checkExists().forPath(path);
        System.out.println(path + (stat == null ? "不存在" : "存在"));
    }

}
