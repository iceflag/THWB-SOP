package com.zyd.sop.gatewaycommon.zuul.configuration;

import com.zyd.sop.gatewaycommon.bean.ApiContext;
import com.zyd.sop.gatewaycommon.manager.AbstractConfiguration;
import com.zyd.sop.gatewaycommon.manager.RouteRepositoryContext;
import com.zyd.sop.gatewaycommon.zuul.filter.ErrorFilter;
import com.zyd.sop.gatewaycommon.zuul.filter.FormBodyWrapperFilterExt;
import com.zyd.sop.gatewaycommon.zuul.filter.PostResultFilter;
import com.zyd.sop.gatewaycommon.zuul.filter.PreHttpServletRequestWrapperFilter;
import com.zyd.sop.gatewaycommon.zuul.filter.PreLimitFilter;
import com.zyd.sop.gatewaycommon.zuul.filter.PreValidateFilter;
import com.zyd.sop.gatewaycommon.zuul.filter.Servlet30WrapperFilterExt;
import com.zyd.sop.gatewaycommon.zuul.route.SopRouteLocator;
import com.zyd.sop.gatewaycommon.zuul.route.ZuulRouteRepository;
import com.zyd.sop.gatewaycommon.zuul.route.ZuulZookeeperRouteManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.cloud.netflix.zuul.filters.ProxyRequestHelper;
import org.springframework.cloud.netflix.zuul.filters.RouteLocator;
import org.springframework.cloud.netflix.zuul.filters.ZuulProperties;
import org.springframework.cloud.netflix.zuul.filters.pre.PreDecorationFilter;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;

/**
 * @author tanghc
 */
public class BaseZuulConfiguration extends AbstractConfiguration {

    @Autowired
    protected ZuulProperties zuulProperties;

    @Autowired
    protected ServerProperties server;

    /**
     * 路由存储
     * @return
     */
    @Bean
    ZuulRouteRepository zuulRouteRepository() {
        ZuulRouteRepository zuulRouteRepository = new ZuulRouteRepository();
        RouteRepositoryContext.setRouteRepository(zuulRouteRepository);
        return zuulRouteRepository;
    }

    @Bean
    PreHttpServletRequestWrapperFilter preHttpServletRequestWrapperFilter() {
        return new PreHttpServletRequestWrapperFilter();
    }

    @Bean
    FormBodyWrapperFilterExt formBodyWrapperFilterExt() {
        return new FormBodyWrapperFilterExt();
    }

    @Bean
    Servlet30WrapperFilterExt servlet30WrapperFilterExt() {
        return new Servlet30WrapperFilterExt();
    }

    /**
     * 选取路由
     * @param zuulRouteRepository
     * @param proxyRequestHelper
     * @return
     */
    @Bean
    PreDecorationFilter preDecorationFilter(ZuulRouteRepository zuulRouteRepository, ProxyRequestHelper proxyRequestHelper) {
        // 自定义路由
        RouteLocator routeLocator = new SopRouteLocator(zuulRouteRepository);
        return new PreDecorationFilter(routeLocator,
                this.server.getServlet().getContextPath(),
                this.zuulProperties,
                proxyRequestHelper);
    }

    /**
     * 路由管理
     * @param environment
     * @param zuulRouteRepository
     */
    @Bean
    ZuulZookeeperRouteManager zuulZookeeperRouteManager(Environment environment, ZuulRouteRepository zuulRouteRepository) {
        return new ZuulZookeeperRouteManager(environment, zuulRouteRepository);
    }

    /**
     * 前置校验
     */
    @Bean
    PreValidateFilter preValidateFilter() {
        return new PreValidateFilter();
    }

    /**
     * 开启限流
     */
    @Bean
    PreLimitFilter preLimitFilter() {
        return new PreLimitFilter();
    }

    /**
     * 错误处理扩展
     */
    @Bean
    ErrorFilter errorFilter() {
        return new ErrorFilter();
    }

    /**
     * 结果返回
     */
    @Bean
    PostResultFilter postResultFilter() {
        return new PostResultFilter();
    }

    /**
     * 统一错误处理
     */
    @Bean
    ZuulErrorController baseZuulController() {
        return ApiContext.getApiConfig().getZuulErrorController();
    }

}
