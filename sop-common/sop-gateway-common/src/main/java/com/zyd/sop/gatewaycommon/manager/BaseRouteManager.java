package com.zyd.sop.gatewaycommon.manager;

import com.alibaba.fastjson.JSON;
import com.zyd.sop.gatewaycommon.bean.BaseRouteDefinition;
import com.zyd.sop.gatewaycommon.bean.BaseServiceRouteInfo;
import com.zyd.sop.gatewaycommon.bean.TargetRoute;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.PathChildrenCacheEvent;
import org.springframework.core.env.Environment;


/**
 * 路由管理，采用zookeeper实现，监听路由的增删改，并适时更新到本地。路由的存储格式为：
 * <pre>
 * /com.zyd.sop.route  根节点
 *      /serviceId       服务节点，名字为服务名
 *          /route1      路由节点，名字为：name+version，存放路由信息
 *          /route2
 *          /...
 * </pre>
 *
 * @param <R> 路由根对象，可以理解为最外面的大json：{....,routeDefinitionList:[]}
 * @param <E> 路由Item对象，对应大json里面的具体路由信息，routeDefinitionList:[]
 * @param <T> 目标路由对象
 * @author tanghc
 */
@Slf4j
public abstract class BaseRouteManager<R extends BaseServiceRouteInfo<E>, E extends BaseRouteDefinition, T extends TargetRoute> implements RouteManager {

    protected Environment environment;

    protected RouteRepository<T> routeRepository;

    protected String routeRootPath;

    /**
     * 返回路由根对象class
     *
     * @return 返回R.class
     */
    protected abstract Class<R> getServiceRouteInfoClass();

    /**
     * 返回路由Item对象class
     *
     * @return 返回E.class
     */
    protected abstract Class<E> getRouteDefinitionClass();

    /**
     * 构建目标路由对象，zuul和gateway定义的路由对象
     *
     * @param serviceRouteInfo
     * @param routeDefinition
     * @return 返回目标路由对象
     */
    protected abstract T buildRouteDefinition(R serviceRouteInfo, E routeDefinition);

    public BaseRouteManager(Environment environment, RouteRepository<T> routeRepository) {
        this.environment = environment;
        ZookeeperContext.setEnvironment(environment);
        this.routeRepository = routeRepository;
        this.routeRootPath = ZookeeperContext.getRouteRootPath();
        this.createRouteRootPath(this.routeRootPath);
    }

    @Override
    public void refresh() {
        this.refreshRouteInfo();
    }

    protected void refreshRouteInfo() {
        try {
            this.watchServiceChange(routeRootPath);
        } catch (Exception e) {
            log.error("刷新路由配置失败", e);
            throw new IllegalStateException("刷新路由配置失败");
        }
    }

    protected void createRouteRootPath(String routeRootPath) {
        try {
            ZookeeperContext.createPath(routeRootPath, "");
        } catch (Exception e) {
            log.error("创建路由根节点失败", e);
            throw new IllegalStateException("创建路由根节点失败");
        }
    }

    /**
     * 监听微服务更改
     *
     * @param rootPath
     * @throws Exception
     */
    protected void watchServiceChange(String rootPath) throws Exception {
        ZookeeperContext.getChildrenAndListen(rootPath, childDataList -> {
            log.info("========== 加载路由信息 ==========");
            log.info("{}  # 路由根节点", rootPath);
            for (ChildData childData : childDataList) {
                String serviceNodeData = new String(childData.getData());
                R serviceRouteInfo = JSON.parseObject(serviceNodeData, getServiceRouteInfoClass());
                String servicePath = childData.getPath();
                log.info("\t{}  # service节点，节点数据:{}", servicePath, serviceNodeData);
                try {
                    this.loadServiceRouteItem(serviceRouteInfo, servicePath);
                } catch (Exception e) {
                    log.error("加载路由信息失败，servicePath:{}", servicePath, e);
                }
            }
        }, (client, event) -> {
            PathChildrenCacheEvent.Type type = event.getType();
            synchronized (type) {
                // 通过判断event type的方式来实现不同事件的触发
                if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(type)) {
                    String serviceNodeData = new String(event.getData().getData());
                    R serviceRouteInfo = JSON.parseObject(serviceNodeData, getServiceRouteInfoClass());
                    // 添加子节点时触发
                    String servicePath = event.getData().getPath();
                    log.info("新增serviceId节点：{}，节点数据:{}", servicePath, serviceNodeData);
                    this.watchRouteItems(serviceRouteInfo, servicePath);
                } else if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(type)) {
                    // 修改子节点数据时触发，暂时没有什么操作
                    String nodeData = new String(event.getData().getData());
                    log.info("修改serviceId节点：{}，节点数据:{}", event.getData().getPath(), nodeData);
                } else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(type)) {
                    // 删除service节点
                    String nodeData = new String(event.getData().getData());
                    log.info("删除serviceId节点：{}，节点数据:{}", event.getData().getPath(), nodeData);
                    R serviceRouteInfo = JSON.parseObject(nodeData, getServiceRouteInfoClass());
                    routeRepository.deleteAll(serviceRouteInfo.getServiceId());
                }
            }
        });
    }

    /**
     * 加载service下的路由信息
     *
     * @param servicePath
     */
    protected void loadServiceRouteItem(R serviceRouteInfo, String servicePath) throws Exception {
        ZookeeperContext.getChildrenData(servicePath, childDataList -> {
            for (ChildData childData : childDataList) {
                String routeItemPath = childData.getPath();
                byte[] routeItemData = childData.getData();
                String routeDataJson = buildZookeeperData(routeItemData);
                log.info("\t\t{}  # 路由节点，节点数据:{}", routeItemPath, routeDataJson);
                this.saveRouteItem(serviceRouteInfo, routeDataJson);
            }
        });
        this.watchRouteItems(serviceRouteInfo, servicePath);
    }


    /**
     * 监听serviceId目录下面的子节点
     *
     * @param serviceRouteInfo
     * @param servicePath      serviceId节点
     */
    protected void watchRouteItems(R serviceRouteInfo, String servicePath) throws Exception {
        log.info("监听{}下子节点增删改", servicePath);
        // 添加事件监听器
        ZookeeperContext.listenChildren(servicePath, (client, event) -> {
            PathChildrenCacheEvent.Type type = event.getType();
            synchronized (type) {
                // 通过判断event type的方式来实现不同事件的触发
                if (PathChildrenCacheEvent.Type.CHILD_ADDED.equals(type)) {
                    // 新增单个路由
                    String routeDataJson = buildZookeeperData(event.getData().getData());
                    log.info("新增单个路由，serviceId:{}, 路由数据:{}", serviceRouteInfo.getServiceId(), routeDataJson);
                    saveRouteItem(serviceRouteInfo, routeDataJson);
                } else if (PathChildrenCacheEvent.Type.CHILD_UPDATED.equals(type)) {
                    // 修改单个路由
                    String routeDataJson = buildZookeeperData(event.getData().getData());
                    log.info("修改单个路由，serviceId:{}, 路由数据:{}", serviceRouteInfo.getServiceId(), routeDataJson);
                    updateRouteItem(serviceRouteInfo, routeDataJson);
                } else if (PathChildrenCacheEvent.Type.CHILD_REMOVED.equals(type)) {
                    // 删除单个路由
                    String routeDataJson = buildZookeeperData(event.getData().getData());
                    log.info("删除单个路由，serviceId:{}, 路由数据:{}", serviceRouteInfo.getServiceId(), routeDataJson);
                    deleteRouteItem(serviceRouteInfo, routeDataJson);
                }
            }
        });

    }

    protected void saveRouteItem(R serviceRouteInfo, String nodeDataJson) {
        T routeDefinition = getRouteDefinition(serviceRouteInfo, nodeDataJson);
        routeRepository.add(routeDefinition);
    }

    protected void updateRouteItem(R serviceRouteInfo, String nodeDataJson) {
        T routeDefinition = getRouteDefinition(serviceRouteInfo, nodeDataJson);
        routeRepository.update(routeDefinition);
    }

    protected void deleteRouteItem(R serviceRouteInfo, String nodeDataJson) {
        E routeDefinitionItem = getRouteDefinitionItem(nodeDataJson);
        routeRepository.delete(routeDefinitionItem.getId());
    }

    protected T getRouteDefinition(R serviceRouteInfo, String nodeDataJson) {
        E routeDefinitionItem = getRouteDefinitionItem(nodeDataJson);
        T routeDefinition = buildRouteDefinition(serviceRouteInfo, routeDefinitionItem);
        return routeDefinition;
    }

    protected E getRouteDefinitionItem(String nodeDataJson) {
        return JSON.parseObject(nodeDataJson, getRouteDefinitionClass());
    }

    protected String buildZookeeperData(byte[] data) {
        return new String(data);
    }

}
