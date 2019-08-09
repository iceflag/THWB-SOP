package com.zyd.sop.gatewaycommon.gateway.configuration;

import com.zyd.sop.gatewaycommon.bean.ApiContext;
import com.zyd.sop.gatewaycommon.validate.alipay.AlipaySigner;

/**
 * 具备支付宝开放平台能力配置 https://docs.open.alipay.com/api
 *
 * @author tanghc
 */
public class AlipayGatewayConfiguration extends BaseGatewayConfiguration {

    static {
        ApiContext.getApiConfig().setSigner(new AlipaySigner());
    }
}
