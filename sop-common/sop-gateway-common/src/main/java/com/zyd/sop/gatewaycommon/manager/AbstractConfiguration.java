package com.zyd.sop.gatewaycommon.manager;

import com.zyd.sop.gatewaycommon.bean.ApiConfig;
import com.zyd.sop.gatewaycommon.bean.ApiContext;
import com.zyd.sop.gatewaycommon.bean.BeanInitializer;
import com.zyd.sop.gatewaycommon.message.ErrorFactory;
import com.zyd.sop.gatewaycommon.secret.IsvManager;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import javax.annotation.PostConstruct;

/**
 * @author tanghc
 */
public class AbstractConfiguration implements ApplicationContextAware {
    @Autowired
    protected Environment environment;

    @Autowired
    protected RouteManager apiMetaManager;

    protected ApplicationContext applicationContext;

    @Override
    public void setApplicationContext(ApplicationContext ctx) throws BeansException {
        applicationContext = ctx;
    }

    @Bean
    IsvManager isvManager() {
        return ApiConfig.getInstance().getIsvManager();
    }

    @Bean
    IsvRoutePermissionManager isvRoutePermissionManager() {
        return ApiConfig.getInstance().getIsvRoutePermissionManager();
    }

    @Bean
    RouteConfigManager routeConfigManager() {
        return ApiConfig.getInstance().getRouteConfigManager();
    }

    @Bean
    LimitConfigManager limitConfigManager() {
        return ApiConfig.getInstance().getLimitConfigManager();
    }

    @Bean
    IPBlacklistManager ipBlacklistManager() {
        return ApiConfig.getInstance().getIpBlacklistManager();
    }

    /**
     * 跨域过滤器
     *
     * @return
     */
    @Bean
    public CorsFilter corsFilter() {
        return createCorsFilter();
    }

    protected CorsFilter createCorsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        this.registerCorsConfiguration(source);
        return new CorsFilter(source);
    }

    protected void registerCorsConfiguration(UrlBasedCorsConfigurationSource source) {
        source.registerCorsConfiguration("/**", corsConfiguration());
    }

    protected CorsConfiguration corsConfiguration() {
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.addAllowedOrigin("*");
        corsConfiguration.addAllowedHeader("*");
        corsConfiguration.addAllowedMethod("*");
        return corsConfiguration;
    }

    @PostConstruct
    public final void after() {
        if (RouteRepositoryContext.getRouteRepository() == null) {
            throw new IllegalArgumentException("RouteRepositoryContext.setRouteRepository()方法未使用");
        }
        EnvironmentContext.setEnvironment(environment);
        ZookeeperContext.setEnvironment(environment);

        initMessage();
        initBeanInitializer();
        apiMetaManager.refresh();
        doAfter();

    }

    protected void initBeanInitializer() {
        String[] beanNames = applicationContext.getBeanNamesForType(BeanInitializer.class);
        if (beanNames != null) {
            for (String beanName : beanNames) {
                BeanInitializer beanInitializer = applicationContext.getBean(beanName, BeanInitializer.class);
                beanInitializer.load();
            }
        }
    }

    protected void doAfter() {

    }

    protected void initMessage() {
        ErrorFactory.initMessageSource(ApiContext.getApiConfig().getI18nModules());
    }
}
