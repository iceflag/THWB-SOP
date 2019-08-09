package com.zyd.sop.servercommon.configuration;

import com.zyd.sop.servercommon.bean.ServiceConfig;
import com.zyd.sop.servercommon.exception.ServiceException;
import com.zyd.sop.servercommon.result.ServiceResultBuilder;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * 全局异常处理
 * @author tanghc
 */
@ControllerAdvice
@Slf4j
public class DefaultGlobalExceptionHandler implements GlobalExceptionHandler {

    /**
     * 与网关约定好的状态码，表示业务出错
     */
    public static final int BIZ_ERROR_CODE = 4000;
    /**
     * 系统错误
     */
    public static final int SYSTEM_ERROR_CODE = 5050;

    public static final String X_SERVICE_ERROR_CODE = "x-service-error-code";

    @RequestMapping("/error")
    @ResponseBody
    public Object error(HttpServletRequest request, HttpServletResponse response) {
        ServiceResultBuilder serviceResultBuilder = ServiceConfig.getInstance().getServiceResultBuilder();
        return serviceResultBuilder.buildError(request, response, new ServiceException("系统繁忙"));
    }

    /**
     * 捕获手动抛出的异常
     * @param request
     * @param response
     * @param exception
     * @return
     */
    @ExceptionHandler(ServiceException.class)
    @ResponseBody
    public Object serviceExceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.addHeader(X_SERVICE_ERROR_CODE, String.valueOf(BIZ_ERROR_CODE));
        return this.processError(request, response, exception);
    }

    /**
     * 捕获未知异常
     * @param request
     * @param response
     * @param exception
     * @return
     */
    @ExceptionHandler(Exception.class)
    @ResponseBody
    public Object exceptionHandler(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        response.addHeader(X_SERVICE_ERROR_CODE, String.valueOf(SYSTEM_ERROR_CODE));
        log.error("系统错误", exception);
        StringBuilder msg = new StringBuilder();
        msg.append(exception.getMessage());
        StackTraceElement[] stackTrace = exception.getStackTrace();
        // 取5行错误内容
        int lineCount = 5;
        for (int i = 0; i < stackTrace.length && i < lineCount ; i++) {
            StackTraceElement stackTraceElement = stackTrace[i];
            msg.append("<br> at ").append(stackTraceElement.toString());
        }
        response.setHeader("x-service-error-message", msg.toString());
        return this.processError(request, response, new ServiceException("系统繁忙"));
    }

    /**
     * 处理异常
     *
     * @param request
     * @param response
     * @param exception
     * @return 返回最终结果
     */
    protected Object processError(HttpServletRequest request, HttpServletResponse response, Exception exception) {
        ServiceResultBuilder serviceResultBuilder = ServiceConfig.getInstance().getServiceResultBuilder();
        return serviceResultBuilder.buildError(request, response, exception);
    }
}
