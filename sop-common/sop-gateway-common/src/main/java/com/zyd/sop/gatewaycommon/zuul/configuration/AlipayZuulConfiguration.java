package com.zyd.sop.gatewaycommon.zuul.configuration;

import com.zyd.sop.gatewaycommon.bean.ApiContext;
import com.zyd.sop.gatewaycommon.validate.alipay.AlipaySigner;

/**
 * 具备支付宝开放平台能力配置 https://docs.open.alipay.com/api
 *
 * @author tanghc
 */
public class AlipayZuulConfiguration extends BaseZuulConfiguration {

    static {
        ApiContext.getApiConfig().setSigner(new AlipaySigner());
    }
}
