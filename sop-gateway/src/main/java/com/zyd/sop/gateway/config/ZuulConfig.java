package com.zyd.sop.gateway.config;

/**
 * 使用Spring Cloud Zuul，推荐使用
 *
 * 注意：下面两个只能使用一个
 */

import com.zyd.sop.gateway.manager.ManagerInitializer;
import com.zyd.sop.gatewaycommon.zuul.configuration.AlipayZuulConfiguration;
import org.springframework.context.annotation.Configuration;

/**
 * 开通支付宝开放平台能力
 * @author tanghc
 */
@Configuration
public class ZuulConfig extends AlipayZuulConfiguration {

    static {
        new ManagerInitializer();
    }

}

/**
 * 开通淘宝开放平台能力
 * @author tanghc
 */
//@Configuration
//public class ZuulConfig extends TaobaoZuulConfiguration {
//  static {
//        new ManagerInitializer();
//        }
//}

/**
 * 对接easyopen
 */
//@Configuration
//public class ZuulConfig extends EasyopenZuulConfiguration {
//    static {
//        new ManagerInitializer();
//        Map<String, String> appSecretMap = new HashMap<>();
//        appSecretMap.put("easyopen_test", "G9w0BAQEFAAOCAQ8AMIIBCgKCA");
//        ApiConfig.getInstance().addAppSecret(appSecretMap);
//    }
//
//}
