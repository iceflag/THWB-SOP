package com.zyd.sop.gatewaycommon.bean;

import com.zyd.sop.gatewaycommon.gateway.param.GatewayParamBuilder;
import com.zyd.sop.gatewaycommon.gateway.result.GatewayResult;
import com.zyd.sop.gatewaycommon.gateway.result.GatewayResultExecutor;
import com.zyd.sop.gatewaycommon.limit.DefaultLimitManager;
import com.zyd.sop.gatewaycommon.limit.LimitManager;
import com.zyd.sop.gatewaycommon.manager.DefaultIPBlacklistManager;
import com.zyd.sop.gatewaycommon.manager.DefaultIsvRoutePermissionManager;
import com.zyd.sop.gatewaycommon.manager.DefaultLimitConfigManager;
import com.zyd.sop.gatewaycommon.manager.DefaultRouteConfigManager;
import com.zyd.sop.gatewaycommon.manager.DefaultServiceErrorManager;
import com.zyd.sop.gatewaycommon.manager.IPBlacklistManager;
import com.zyd.sop.gatewaycommon.manager.IsvRoutePermissionManager;
import com.zyd.sop.gatewaycommon.manager.LimitConfigManager;
import com.zyd.sop.gatewaycommon.manager.RouteConfigManager;
import com.zyd.sop.gatewaycommon.manager.ServiceErrorManager;
import com.zyd.sop.gatewaycommon.param.ParamBuilder;
import com.zyd.sop.gatewaycommon.result.DataNameBuilder;
import com.zyd.sop.gatewaycommon.result.DefaultDataNameBuilder;
import com.zyd.sop.gatewaycommon.result.ResultAppender;
import com.zyd.sop.gatewaycommon.result.ResultExecutor;
import com.zyd.sop.gatewaycommon.secret.CacheIsvManager;
import com.zyd.sop.gatewaycommon.secret.IsvManager;
import com.zyd.sop.gatewaycommon.session.ApiSessionManager;
import com.zyd.sop.gatewaycommon.session.SessionManager;
import com.zyd.sop.gatewaycommon.validate.ApiEncrypter;
import com.zyd.sop.gatewaycommon.validate.ApiSigner;
import com.zyd.sop.gatewaycommon.validate.ApiValidator;
import com.zyd.sop.gatewaycommon.validate.Encrypter;
import com.zyd.sop.gatewaycommon.validate.Signer;
import com.zyd.sop.gatewaycommon.validate.Validator;
import com.zyd.sop.gatewaycommon.zuul.configuration.ZuulErrorController;
import com.zyd.sop.gatewaycommon.zuul.param.ZuulParamBuilder;
import com.zyd.sop.gatewaycommon.zuul.result.ZuulResultExecutor;
import com.netflix.zuul.context.RequestContext;
import lombok.Data;
import org.springframework.web.server.ServerWebExchange;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author tanghc
 */
@Data
public class ApiConfig {

    private static ApiConfig instance = new ApiConfig();

    private ApiConfig() {
    }

    /**
     * gateway合并结果处理
     */
    private ResultExecutor<ServerWebExchange, GatewayResult> gatewayResultExecutor = new GatewayResultExecutor();

    /**
     * zuul合并结果处理
     */
    private ResultExecutor<RequestContext, String> zuulResultExecutor = new ZuulResultExecutor();

    /**
     * isv管理
     */
    private IsvManager isvManager = new CacheIsvManager();

    /**
     * 加密工具
     */
    private Encrypter encrypter = new ApiEncrypter();

    /**
     * 签名工具
     */
    private Signer signer = new ApiSigner();

    /**
     * 参数解析，gateway
     */
    private ParamBuilder<ServerWebExchange> gatewayParamBuilder = new GatewayParamBuilder();

    /**
     * 参数解析，zuul
     */
    private ParamBuilder<RequestContext> zuulParamBuilder = new ZuulParamBuilder();

    /**
     * 验证
     */
    private Validator validator = new ApiValidator();

    /**
     * session管理
     */
    private SessionManager sessionManager = new ApiSessionManager();

    /**
     * zuul网关全局异常处理
     */
    private ZuulErrorController zuulErrorController = new ZuulErrorController();

    /**
     * isv路由权限
     */
    private IsvRoutePermissionManager isvRoutePermissionManager = new DefaultIsvRoutePermissionManager();

    /**
     * 路由配置管理
     */
    private RouteConfigManager routeConfigManager = new DefaultRouteConfigManager();

    /**
     * 限流配置
     */
    private LimitConfigManager limitConfigManager = new DefaultLimitConfigManager();

    /**
     * IP黑名单
     */
    private IPBlacklistManager ipBlacklistManager = new DefaultIPBlacklistManager();

    /**
     * 限流管理
     */
    private LimitManager limitManager = new DefaultLimitManager();

    /**
     * 构建数据节点名称
     */
    private DataNameBuilder dataNameBuilder = new DefaultDataNameBuilder();

    /**
     * 追加结果
     */
    private ResultAppender resultAppender;

    /**
     * 处理错误信息
     */
    private ServiceErrorManager serviceErrorManager = new DefaultServiceErrorManager();

    // -------- fields ---------

    /**
     * 错误模块
     */
    private List<String> i18nModules = new ArrayList<>();


    /**
     * 忽略验证，设置true，则所有接口不会进行签名校验
     */
    private boolean ignoreValidate;

    /**
     * 是否对结果进行合并。<br>
     * 默认情况下是否合并结果由微服务端决定，一旦指定该值，则由该值决定，不管微服务端如何设置。
     */
    private Boolean mergeResult;

    /**
     * 超时时间
     */
    private int timeoutSeconds = 60 * 5;

    /**
     * 是否开启限流功能
     */
    private boolean openLimit = true;

    /**
     * 显示返回sign
     */
    private boolean showReturnSign = true;

    /**
     * 保存错误信息容器的容量
     */
    private int storeErrorCapacity = 20;

    private boolean useGateway;

    public void addAppSecret(Map<String, String> appSecretPair) {
        for (Map.Entry<String, String> entry : appSecretPair.entrySet()) {
            this.isvManager.update(new IsvDefinition(entry.getKey(), entry.getValue()));
        }
    }

    public static ApiConfig getInstance() {
        return instance;
    }

    public static void setInstance(ApiConfig apiConfig) {
        instance = apiConfig;
    }


}
