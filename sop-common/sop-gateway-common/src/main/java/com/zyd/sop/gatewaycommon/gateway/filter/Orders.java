package com.zyd.sop.gatewaycommon.gateway.filter;

import org.springframework.core.Ordered;

/**
 * @author tanghc
 */
public class Orders {
    /** 验证拦截器order */
    public static final int VALIDATE_ORDER = Ordered.HIGHEST_PRECEDENCE + 1000;

    /** 验证拦截器order */
    public static final int LIMIT_ORDER = VALIDATE_ORDER + 1;
}
