package com.zyd.sop.gatewaycommon.zuul.configuration;

import com.zyd.sop.gatewaycommon.bean.ApiConfig;
import com.zyd.sop.gatewaycommon.result.CustomDataNameBuilder;

/**
 * 支持传统webapp开发，没有签名验证
 *
 * @author tanghc
 */
public class WebappZuulConfiguration extends BaseZuulConfiguration {

    static {
        ApiConfig.getInstance().setDataNameBuilder(new CustomDataNameBuilder());
        ApiConfig.getInstance().setShowReturnSign(false);
    }
}
