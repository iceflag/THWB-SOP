package com.zyd.sop.gatewaycommon.validate;

import com.zyd.sop.gatewaycommon.param.ApiParam;

/**
 * 负责签名校验
 * @author tanghc
 *
 */
public interface Signer {

    /**
     * 签名校验
     * @param apiParam 参数
     * @param secret 秘钥
     * @return true签名正确
     */
    boolean checkSign(ApiParam apiParam, String secret);
    
}
