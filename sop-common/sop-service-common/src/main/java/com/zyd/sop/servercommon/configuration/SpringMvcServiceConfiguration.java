package com.zyd.sop.servercommon.configuration;

import com.zyd.sop.servercommon.bean.ServiceConfig;
import com.zyd.sop.servercommon.manager.ApiMetaManager;
import com.zyd.sop.servercommon.manager.DefaultRequestMappingEvent;
import com.zyd.sop.servercommon.manager.RequestMappingEvent;
import com.zyd.sop.servercommon.manager.ServiceZookeeperApiMetaManager;
import com.zyd.sop.servercommon.mapping.ApiMappingHandlerMapping;
import com.zyd.sop.servercommon.message.ServiceErrorFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;

import javax.annotation.PostConstruct;
import java.util.concurrent.Executors;

/**
 * 提供给springmvc工程
 * @author tanghc
 */
@Slf4j
public class SpringMvcServiceConfiguration {

    public SpringMvcServiceConfiguration() {
        ServiceConfig.getInstance().getI18nModules().add("i18n/isp/bizerror");
    }

    private ApiMappingHandlerMapping apiMappingHandlerMapping = new ApiMappingHandlerMapping();

    @Autowired
    private Environment environment;


    /**
     * 自定义Mapping，详见@ApiMapping
     *
     * @return 返回RequestMappingHandlerMapping
     */
    @Bean
    @Primary
    public RequestMappingHandlerMapping requestMappingHandlerMapping() {
        return apiMappingHandlerMapping;
    }


    @Bean
    GlobalExceptionHandler globalExceptionHandler() {
        return ServiceConfig.getInstance().getGlobalExceptionHandler();
    }

    @PostConstruct
    public final void after() {
        log.info("-----spring容器加载完毕-----");
        Executors.newSingleThreadExecutor().execute(()->{
            uploadRouteToZookeeper();
        });
        initMessage();
        doAfter();
    }

    private void uploadRouteToZookeeper() {
        ApiMetaManager apiMetaManager = new ServiceZookeeperApiMetaManager(environment);
        RequestMappingEvent requestMappingEvent = new DefaultRequestMappingEvent(apiMetaManager, environment);
        requestMappingEvent.onRegisterSuccess(apiMappingHandlerMapping);
    }


    /**
     * spring容器加载完毕后执行
     */
    protected void doAfter() {

    }

    protected void initMessage() {
        ServiceErrorFactory.initMessageSource(ServiceConfig.getInstance().getI18nModules());
    }

}
