package com.zyd.sop.servercommon.configuration;

import com.zyd.sop.servercommon.bean.ServiceConfig;
import com.zyd.sop.servercommon.interceptor.ServiceContextInterceptor;
import com.zyd.sop.servercommon.manager.ApiMetaManager;
import com.zyd.sop.servercommon.manager.DefaultRequestMappingEvent;
import com.zyd.sop.servercommon.manager.RequestMappingEvent;
import com.zyd.sop.servercommon.manager.ServiceZookeeperApiMetaManager;
import com.zyd.sop.servercommon.mapping.ApiMappingHandlerMapping;
import com.zyd.sop.servercommon.message.ServiceErrorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;

/**
 * @author tanghc
 */
@Slf4j
public class BaseServiceConfiguration extends WebMvcConfigurationSupport
        implements ApplicationRunner {

    public BaseServiceConfiguration() {
        ServiceConfig.getInstance().getI18nModules().add("i18n/isp/bizerror");
    }

    @Autowired
    private Environment environment;

    private ApiMappingHandlerMapping apiMappingHandlerMapping = new ApiMappingHandlerMapping();


    @Override
    protected void addInterceptors(InterceptorRegistry registry) {
        // 添加拦截器
        registry.addInterceptor(new ServiceContextInterceptor());
        super.addInterceptors(registry);
    }

    /**
     * 自定义Mapping，详见@ApiMapping
     * @return 返回RequestMappingHandlerMapping
     */
    @Override
    protected RequestMappingHandlerMapping createRequestMappingHandlerMapping() {
        return apiMappingHandlerMapping;
    }

    protected RequestMappingEvent getRequestMappingEvent(ApiMetaManager apiMetaManager, Environment environment) {
        return new DefaultRequestMappingEvent(apiMetaManager, environment);
    }

    protected ApiMetaManager getApiMetaManager(Environment environment) {
        return new ServiceZookeeperApiMetaManager(environment);
    }

    @Bean
    GlobalExceptionHandler globalExceptionHandler() {
        return ServiceConfig.getInstance().getGlobalExceptionHandler();
    }

    @PostConstruct
    public final void after() {
        log.info("-----spring容器加载完毕-----");
        initMessage();
        doAfter();
    }

    /**
     * springboot启动完成后执行
     * @param args 启动参数
     * @throws Exception 出错异常
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("-----服务器启动完毕-----");
        ApiMetaManager apiMetaManager = getApiMetaManager(environment);
        RequestMappingEvent requestMappingEvent = getRequestMappingEvent(apiMetaManager, environment);
        requestMappingEvent.onRegisterSuccess(apiMappingHandlerMapping);
        this.onStartup(args);
    }

    /**
     * spring容器加载完毕后执行
     */
    protected void doAfter() {

    }

    /**
     * 启动完毕后执行
     * @param args
     */
    protected void onStartup(ApplicationArguments args) {

    }

    protected void initMessage() {
        ServiceErrorFactory.initMessageSource(ServiceConfig.getInstance().getI18nModules());
    }

}
