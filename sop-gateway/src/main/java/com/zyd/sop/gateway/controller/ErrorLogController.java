package com.zyd.sop.gateway.controller;

import com.zyd.sop.gatewaycommon.bean.ApiConfig;
import com.zyd.sop.gatewaycommon.bean.ErrorEntity;
import com.zyd.sop.gatewaycommon.manager.ServiceErrorManager;
import com.zyd.sop.gatewaycommon.param.ApiParam;
import com.zyd.sop.gatewaycommon.result.ApiResult;
import com.zyd.sop.gatewaycommon.result.JsonResult;
import com.zyd.sop.gatewaycommon.util.RequestUtil;
import com.zyd.sop.gatewaycommon.validate.taobao.TaobaoSigner;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.Collection;
import java.util.Map;

/**
 * @author tanghc
 */
@RestController
public class ErrorLogController {

    TaobaoSigner signer = new TaobaoSigner();

    @Value("${zuul.secret}")
    private String secret;

    @GetMapping("listErrors")
    public ApiResult listErrors(HttpServletRequest request) {
        try {
            this.check(request);
            ServiceErrorManager serviceErrorManager = ApiConfig.getInstance().getServiceErrorManager();
            Collection<ErrorEntity> allErrors = serviceErrorManager.listAllErrors();
            JsonResult apiResult = new JsonResult();
            apiResult.setData(allErrors);
            return apiResult;
        } catch (Exception e) {
            ApiResult apiResult = new ApiResult();
            apiResult.setCode("505050");
            apiResult.setMsg(e.getMessage());
            return apiResult;
        }
    }

    @GetMapping("clearErrors")
    public ApiResult clearErrors(HttpServletRequest request) {
        try {
            this.check(request);
            ServiceErrorManager serviceErrorManager = ApiConfig.getInstance().getServiceErrorManager();
            serviceErrorManager.clear();
            return new ApiResult();
        } catch (Exception e) {
            ApiResult apiResult = new ApiResult();
            apiResult.setCode("505050");
            apiResult.setMsg(e.getMessage());
            return apiResult;
        }
    }

    private void check(HttpServletRequest request) {
        Map<String, String> params = RequestUtil.convertRequestParamsToMap(request);
        ApiParam apiParam = ApiParam.build(params);
        signer.checkSign(apiParam, secret);
    }

}
