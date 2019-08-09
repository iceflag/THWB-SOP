package com.zyd.sop.servercommon.swagger;

import org.apache.commons.lang3.StringUtils;
import org.springframework.util.DigestUtils;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author tanghc
 */
public class SwaggerValidator {

    private String secret = "b749a2ec000f4f29";

    private boolean swaggerAccessProtected = true;

    public SwaggerValidator(boolean swaggerAccessProtected) {
        this.swaggerAccessProtected = swaggerAccessProtected;
    }

    public SwaggerValidator() {
    }

    /**
     * swagger访问是否加密保护
     * @return
     */
    public boolean swaggerAccessProtected() {
        return swaggerAccessProtected;
    }

    public boolean validate(HttpServletRequest request) {
        String time = request.getParameter("time");
        String sign = request.getParameter("sign");
        if (StringUtils.isAnyBlank(time, sign)) {
            return false;
        }
        String source = secret + time + secret;
        String serverSign = DigestUtils.md5DigestAsHex(source.getBytes());
        return serverSign.equals(sign);
    }

    public void writeForbidden(HttpServletResponse response) throws IOException {
        response.setContentType("text/palin;charset=UTF-8");
        response.setStatus(403);
        PrintWriter printWriter = response.getWriter();
        printWriter.write("access forbidden");
        printWriter.flush();
    }
}
