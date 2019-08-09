package com.zyd.sop.servercommon.configuration;

import com.zyd.sop.servercommon.bean.ServiceConfig;
import com.zyd.sop.servercommon.param.SopHandlerMethodArgumentResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;

/**
 * 具备支付宝开放平台服务提供能力
 * @author tanghc
 */
public class AlipayServiceConfiguration extends BaseServiceConfiguration {

    static {
        // 默认版本号为1.0
        ServiceConfig.getInstance().setDefaultVersion("1.0");
    }

    @Autowired
    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    protected void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        super.addArgumentResolvers(argumentResolvers);
        SopHandlerMethodArgumentResolver sopHandlerMethodArgumentResolver = ServiceConfig.getInstance().getMethodArgumentResolver();
        argumentResolvers.add(sopHandlerMethodArgumentResolver);
    }

    @Override
    protected void doAfter() {
        super.doAfter();
        SopHandlerMethodArgumentResolver sopHandlerMethodArgumentResolver = ServiceConfig.getInstance().getMethodArgumentResolver();
        sopHandlerMethodArgumentResolver.setRequestMappingHandlerAdapter(requestMappingHandlerAdapter);
    }
}
