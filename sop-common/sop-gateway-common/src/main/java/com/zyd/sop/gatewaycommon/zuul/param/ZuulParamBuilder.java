package com.zyd.sop.gatewaycommon.zuul.param;

import com.zyd.sop.gatewaycommon.bean.SopConstants;
import com.zyd.sop.gatewaycommon.param.ApiParam;
import com.zyd.sop.gatewaycommon.param.BaseParamBuilder;
import com.zyd.sop.gatewaycommon.util.RequestUtil;
import com.netflix.zuul.context.RequestContext;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * 参数解析默认实现
 *
 * @author tanghc
 */
@Slf4j
public class ZuulParamBuilder extends BaseParamBuilder<RequestContext> {

    private static final String CONTENT_TYPE_JSON = MediaType.APPLICATION_JSON_VALUE;
    private static final String CONTENT_TYPE_TEXT = MediaType.TEXT_PLAIN_VALUE;
    private static final String GET = "get";

    @Override
    public Map<String, String> buildRequestParams(RequestContext ctx) {
        HttpServletRequest request = ctx.getRequest();
        Map<String, String> params;
        if (GET.equalsIgnoreCase(request.getMethod())) {
            params = RequestUtil.convertRequestParamsToMap(request);
        } else {
            String contentType = request.getContentType();
            if (contentType == null) {
                contentType = "";
            }
            contentType = contentType.toLowerCase();
            // json或者纯文本形式
            if (contentType.contains(CONTENT_TYPE_JSON) || contentType.contains(CONTENT_TYPE_TEXT)) {
                params = RequestUtil.convertJsonRequestToMap(request);
            } else if (ServletFileUpload.isMultipartContent(request)) {
                params = RequestUtil.convertMultipartRequestToMap(request);
            } else {
                params = RequestUtil.convertRequestParamsToMap(request);
            }
        }
        return params;
    }

    @Override
    public String getIP(RequestContext ctx) {
        return RequestUtil.getIP(ctx.getRequest());
    }

    @Override
    protected ApiParam newApiParam(RequestContext ctx) {
        ApiParam apiParam = super.newApiParam(ctx);
        HttpServletRequest request = ctx.getRequest();
        String method = (String) request.getAttribute(SopConstants.REDIRECT_METHOD_KEY);
        String version = (String) request.getAttribute(SopConstants.REDIRECT_VERSION_KEY);
        apiParam.setRestName(method);
        apiParam.setRestVersion(version);
        return apiParam;
    }

}
