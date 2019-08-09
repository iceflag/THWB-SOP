package com.zyd.sop.gatewaycommon.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.InetAddress;
import java.net.URLDecoder;
import java.net.UnknownHostException;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;


/**
 * @author tanghc
 */
public class RequestUtil {

    private static Logger log = LoggerFactory.getLogger(RequestUtil.class);

    private RequestUtil() {
    }

    public static final String MULTIPART = "multipart/";

    private static final String UTF8 = "UTF-8";
    private static final String IP_UNKNOWN = "unknown";
    private static final String IP_LOCAL = "127.0.0.1";
    private static final int IP_LEN = 15;

    /**
     * 将get类型的参数转换成map，
     *
     * @param query charset=utf-8&biz_content=xxx
     * @return 返回map参数
     */
    public static Map<String, String> parseQueryToMap(String query) {
        if (query == null) {
            return Collections.emptyMap();
        }
        String[] queryList = StringUtils.split(query, '&');
        Map<String, String> params = new HashMap<>(16);
        for (String param : queryList) {
            String[] paramArr = param.split("\\=");
            if (paramArr.length == 2) {
                try {
                    params.put(paramArr[0], URLDecoder.decode(paramArr[1], UTF8));
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException(e);
                }
            } else if (paramArr.length == 1) {
                params.put(paramArr[0], "");
            }
        }
        return params;
    }

    /**
     * request中的参数转换成map
     *
     * @param request request对象
     * @return 返回参数键值对
     */
    public static Map<String, String> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap == null || paramMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, String> retMap = new HashMap<>(paramMap.size());

        Set<Map.Entry<String, String[]>> entrySet = paramMap.entrySet();

        for (Map.Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values.length >= 1) {
                retMap.put(name, values[0]);
            } else {
                retMap.put(name, "");
            }
        }
        return retMap;
    }

    /**
     * 获取文件上传表单中的字段，不包括文件，请求类型是multipart/form-data
     *
     * @param request
     * @return 返回表单中的字段内容
     */
    public static Map<String, String> convertMultipartRequestToMap(HttpServletRequest request) {
        DiskFileItemFactory factory = new DiskFileItemFactory();
        //2、创建一个文件上传解析器
        ServletFileUpload upload = new ServletFileUpload(factory);
        Map<String, String> params = new HashMap<>(16);
        try {
            List<FileItem> fileItems = upload.parseRequest(request);
            for (FileItem fileItem : fileItems) {
                if (fileItem.isFormField()) {
                    params.put(fileItem.getFieldName(), fileItem.getString(UTF8));
                }
            }
        } catch (Exception e) {
            log.error("参数解析错误", e);
        }
        return params;
    }

    /**
     * 转换json请求到Map，
     * @param request 请求类型为application/json的Request
     * @return 返回Map
     */
    public static Map<String, String> convertJsonRequestToMap(HttpServletRequest request) {
        try {
            String text = getText(request);
            JSONObject parseObject = JSON.parseObject(text);
            Map<String, String> params = new HashMap<>(parseObject.size());
            for (Map.Entry<String, Object> entry : parseObject.entrySet()) {
                params.put(entry.getKey(), String.valueOf(entry.getValue()));
            }
            return params;
        } catch (IOException e) {
            log.error("解析json请求失败", e);
            return Collections.emptyMap();
        }
    }

    /**
     * 是否是文件上传请求
     * @param request 请求
     * @return true：是
     */
    public static boolean isMultipart(HttpServletRequest request) {
        String contentType = request.getContentType();
        // Don't use this filter on GET method
        if (contentType == null) {
            return false;
        }
        return contentType.toLowerCase(Locale.ENGLISH).startsWith(MULTIPART);
    }


    public static String getText(HttpServletRequest request) throws IOException {
        return IOUtils.toString(request.getInputStream(), UTF8);
    }

    public static String getIP(HttpServletRequest request) {
        String ipAddress = request.getHeader("x-forwarded-for");
        if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.length() == 0 || IP_UNKNOWN.equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
            if (IP_LOCAL.equals(ipAddress)) {
                // 根据网卡取本机配置的IP
                try {
                    InetAddress inet = InetAddress.getLocalHost();
                    ipAddress = inet.getHostAddress();
                } catch (UnknownHostException e) {
                    // ignore
                }
            }

        }

        // 对于通过多个代理的情况，第一个IP为客户端真实IP,多个IP按照','分割
        if (ipAddress != null && ipAddress.length() > IP_LEN) {
            if (ipAddress.indexOf(",") > 0) {
                ipAddress = ipAddress.substring(0, ipAddress.indexOf(","));
            }
        }
        return ipAddress;
    }
}
