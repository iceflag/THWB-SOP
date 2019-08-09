package com.zyd.sop.registryapi.config;

import com.zyd.sop.registryapi.service.RegistryService;
import com.zyd.sop.registryapi.service.impl.RegistryServiceEureka;
import com.zyd.sop.registryapi.service.impl.RegistryServiceNacos;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

/**
 * @author tanghc
 */
public class BaseRegistryConfig {

    /**
     * 当配置了registry.name=eureka生效
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "registry", name = "name", havingValue = "eureka")
    RegistryService registryServiceEureka() {
        return new RegistryServiceEureka();
    }

    /**
     * 当配置了registry.name=nacos生效
     *
     * @return
     */
    @Bean
    @ConditionalOnProperty(prefix = "registry", name = "name", havingValue = "nacos")
    RegistryService registryServiceNacos() {
        return new RegistryServiceNacos();
    }

}
