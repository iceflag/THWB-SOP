package com.zyd.sop.gateway.controller;

import com.zyd.sop.gatewaycommon.bean.SopConstants;
import com.zyd.sop.gatewaycommon.param.ParamNames;
import com.zyd.sop.gatewaycommon.util.RouteUtil;
import org.springframework.beans.factory.annotation.Value;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 传统web开发入口
 * @author tanghc
 */
@WebServlet(urlPatterns = "/rest/*")
public class RestServlet extends HttpServlet {

    private static final String REST_PATH = "/rest";

    @Value("${zuul.servlet-path:/zuul}")
    private String path;

    @Value("${zuul.rest-default-version:1.0}")
    private String defaultVersion;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doPost(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String url = request.getRequestURL().toString();
        int index = url.indexOf(REST_PATH);
        // 取/rest的后面部分
        String path = url.substring(index + REST_PATH.length());
        String method = RouteUtil.buildApiName(path);
        String version = request.getParameter(ParamNames.VERSION_NAME);
        if (version == null) {
            version = defaultVersion;
        }
        request.setAttribute(SopConstants.REDIRECT_METHOD_KEY, method);
        request.setAttribute(SopConstants.REDIRECT_VERSION_KEY, version);
        request.getRequestDispatcher(this.path).forward(request, response);
    }

}
