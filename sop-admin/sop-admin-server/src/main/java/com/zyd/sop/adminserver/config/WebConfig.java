package com.zyd.sop.adminserver.config;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.gitee.easyopen.ApiConfig;
import com.gitee.easyopen.ApiParam;
import com.gitee.easyopen.ApiParamParser;
import com.gitee.easyopen.ParamNames;
import com.gitee.easyopen.interceptor.ApiInterceptor;
import com.gitee.easyopen.session.ApiSessionManager;
import com.zyd.sop.adminserver.bean.ZookeeperContext;
import com.zyd.sop.adminserver.interceptor.LoginInterceptor;
import org.apache.commons.lang.StringUtils;
import org.apache.commons.lang.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;

import javax.annotation.PostConstruct;
import javax.servlet.http.HttpServletRequest;


/**
 * @author tanghc
 */
@Configuration
public class WebConfig {

    public static final String HEADER_TOKEN_NAME = ParamNames.ACCESS_TOKEN_NAME;

    @Value("${admin.access-token.timeout-minutes}")
    private String accessTokenTimeout;

    @Autowired
    private Environment environment;

    @Bean
    ApiConfig apiConfig() {
        ApiConfig apiConfig = new ApiConfig();
        apiConfig.setJsonResultSerializer(obj -> JSON.toJSONString(obj,
                SerializerFeature.WriteDateUseDateFormat
                , SerializerFeature.WriteNullStringAsEmpty
                , SerializerFeature.WriteMapNullValue));
        ApiSessionManager apiSessionManager = new ApiSessionManager();
        // session有效期
        int timeout = NumberUtils.toInt(accessTokenTimeout, 30);
        apiSessionManager.setSessionTimeout(timeout);
        apiConfig.setSessionManager(apiSessionManager);
        // 登录拦截器
        apiConfig.setInterceptors(new ApiInterceptor[]{new LoginInterceptor()});

        apiConfig.setParamParser(new ApiParamParser() {
            @Override
            public ApiParam parse(HttpServletRequest request) {
                ApiParam param = super.parse(request);
                String accessToken = request.getHeader(HEADER_TOKEN_NAME);
                if (StringUtils.isNotBlank(accessToken)) {
                    param.put(ParamNames.ACCESS_TOKEN_NAME, accessToken);
                }
                return param;
            }
        });

        return apiConfig;
    }

    @PostConstruct
    public void after() {
        ZookeeperContext.setEnvironment(environment);
    }

}
