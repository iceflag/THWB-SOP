package com.zyd.sop.gatewaycommon.gateway.configuration;

import com.zyd.sop.gatewaycommon.bean.ApiConfig;
import com.zyd.sop.gatewaycommon.gateway.filter.GatewayModifyResponseGatewayFilter;
import com.zyd.sop.gatewaycommon.gateway.filter.LoadBalancerClientExtFilter;
import com.zyd.sop.gatewaycommon.gateway.filter.ValidateFilter;
import com.zyd.sop.gatewaycommon.gateway.handler.GatewayExceptionHandler;
import com.zyd.sop.gatewaycommon.gateway.route.GatewayRouteRepository;
import com.zyd.sop.gatewaycommon.gateway.route.GatewayZookeeperRouteManager;
import com.zyd.sop.gatewaycommon.gateway.route.NameVersionRoutePredicateFactory;
import com.zyd.sop.gatewaycommon.gateway.route.ReadBodyRoutePredicateFactory;
import com.zyd.sop.gatewaycommon.manager.AbstractConfiguration;
import com.zyd.sop.gatewaycommon.manager.RouteManager;
import com.zyd.sop.gatewaycommon.manager.RouteRepositoryContext;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.core.env.Environment;
import org.springframework.http.codec.ServerCodecConfigurer;
import org.springframework.web.reactive.result.view.ViewResolver;

import java.util.Collections;
import java.util.List;


/**
 * @author tanghc
 */
public class BaseGatewayConfiguration extends AbstractConfiguration {

    public BaseGatewayConfiguration() {
        ApiConfig.getInstance().setUseGateway(true);
    }

    /**
     * 自定义异常处理[@@]注册Bean时依赖的Bean，会从容器中直接获取，所以直接注入即可
     *
     * @param viewResolversProvider viewResolversProvider
     * @param serverCodecConfigurer serverCodecConfigurer
     */
    @Primary
    @Bean
    @Order(Ordered.HIGHEST_PRECEDENCE)
    public ErrorWebExceptionHandler errorWebExceptionHandler(ObjectProvider<List<ViewResolver>> viewResolversProvider,
                                                             ServerCodecConfigurer serverCodecConfigurer) {

        GatewayExceptionHandler jsonExceptionHandler = new GatewayExceptionHandler();
        jsonExceptionHandler.setViewResolvers(viewResolversProvider.getIfAvailable(Collections::emptyList));
        jsonExceptionHandler.setMessageWriters(serverCodecConfigurer.getWriters());
        jsonExceptionHandler.setMessageReaders(serverCodecConfigurer.getReaders());
        return jsonExceptionHandler;
    }

    /**
     * 处理返回结果
     */
    @Bean
    GatewayModifyResponseGatewayFilter gatewayModifyResponseGatewayFilter() {
        return new GatewayModifyResponseGatewayFilter();
    }

    /**
     * 读取post请求参数
     */
    @Bean
    ReadBodyRoutePredicateFactory readBodyRoutePredicateFactory() {
        return new ReadBodyRoutePredicateFactory();
    }

    @Bean
    NameVersionRoutePredicateFactory paramRoutePredicateFactory() {
        return new NameVersionRoutePredicateFactory();
    }

    @Bean
    ValidateFilter validateFilter() {
        return new ValidateFilter();
    }

    @Bean
    LoadBalancerClientExtFilter loadBalancerClientExtFilter() {
        return new LoadBalancerClientExtFilter();
    }

    @Bean
    RouteManager gatewayZookeeperRouteManager(Environment environment, GatewayRouteRepository gatewayRouteManager) {
        return new GatewayZookeeperRouteManager(environment, gatewayRouteManager);
    }

    @Bean
    GatewayRouteRepository gatewayRouteRepository() {
        GatewayRouteRepository gatewayRouteRepository = new GatewayRouteRepository();
        RouteRepositoryContext.setRouteRepository(gatewayRouteRepository);
        return gatewayRouteRepository;
    }

}
