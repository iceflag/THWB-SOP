package com.zyd.sop.gatewaycommon.zuul.filter;

import com.zyd.sop.gatewaycommon.bean.ApiConfig;
import com.zyd.sop.gatewaycommon.bean.ApiContext;
import com.zyd.sop.gatewaycommon.exception.ApiException;
import com.zyd.sop.gatewaycommon.param.ApiParam;
import com.zyd.sop.gatewaycommon.validate.Validator;
import com.zyd.sop.gatewaycommon.zuul.ZuulContext;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;

/**
 * 前置校验
 *
 * @author tanghc
 */
public class PreValidateFilter extends BaseZuulFilter {
    @Override
    protected FilterType getFilterType() {
        return FilterType.PRE;
    }

    @Override
    protected int getFilterOrder() {
        return PRE_VALIDATE_FILTER_ORDER;
    }

    @Override
    protected Object doRun(RequestContext requestContext) throws ZuulException {
        ApiConfig apiConfig = ApiContext.getApiConfig();
        // 解析参数
        ApiParam param = apiConfig.getZuulParamBuilder().build(requestContext);
        ZuulContext.setApiParam(param);
        // 验证操作，这里有负责验证签名参数
        Validator validator = apiConfig.getValidator();
        try {
            validator.validate(param);
        } catch (ApiException e) {
            log.error("验证失败，ip:{}, params:{}", param.fetchIp(), param.toJSONString(), e);
            throw e;
        } finally {
            param.fitNameVersion();
        }
        return null;
    }

}
