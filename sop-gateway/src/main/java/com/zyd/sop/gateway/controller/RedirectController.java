package com.zyd.sop.gateway.controller;

import com.zyd.sop.gatewaycommon.bean.SopConstants;
import com.zyd.sop.gatewaycommon.param.ParamNames;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * @author tanghc
 */
@Controller
public class RedirectController {

    @Value("${zuul.servlet-path}")
    private String path;

    @RequestMapping("/{method}/{version}/")
    public void redirect(
            @PathVariable("method") String method
            , @PathVariable("version") String version
            , HttpServletRequest request
            , HttpServletResponse response
    ) throws ServletException, IOException {
        request.setAttribute(SopConstants.REDIRECT_METHOD_KEY, method);
        request.setAttribute(SopConstants.REDIRECT_VERSION_KEY, version);
        String queryString = request.getQueryString();
        String versionQuery = ParamNames.VERSION_NAME + '=' + version;
        if (StringUtils.isBlank(queryString)) {
            queryString = versionQuery;
        } else {
            queryString = queryString + '&' + versionQuery;
        }
        request.getRequestDispatcher(path + '?' + queryString).forward(request, response);
    }

}
