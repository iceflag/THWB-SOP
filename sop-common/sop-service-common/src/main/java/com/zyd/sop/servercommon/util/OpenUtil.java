package com.zyd.sop.servercommon.util;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;

import javax.servlet.http.HttpServletRequest;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * @author tanghc
 */
@Slf4j
public class OpenUtil {

    private static final String UTF8 = "UTF-8";

    /**
     * 获取request中的参数
     *
     * @param request request对象
     * @return 返回JSONObject
     */
    public static JSONObject getRequestParams(HttpServletRequest request) {
        String contentType = request.getContentType();
        if (contentType == null) {
            contentType = "";
        }
        contentType = contentType.toLowerCase();
        JSONObject jsonObject;
        if (StringUtils.containsAny(contentType, MediaType.APPLICATION_JSON_VALUE, MediaType.TEXT_PLAIN_VALUE)) {
            try {
                String requestJson = IOUtils.toString(request.getInputStream(), UTF8);
                jsonObject = JSON.parseObject(requestJson);
            } catch (Exception e) {
                jsonObject = new JSONObject();
                log.error("获取文本数据流失败", e);
            }
        } else {
            Map<String, Object> params = convertRequestParamsToMap(request);
            jsonObject = new JSONObject(params);
        }
        return jsonObject;
    }


    /**
     * request中的参数转换成map
     *
     * @param request request对象
     * @return 返回参数键值对
     */
    public static Map<String, Object> convertRequestParamsToMap(HttpServletRequest request) {
        Map<String, String[]> paramMap = request.getParameterMap();
        if (paramMap == null || paramMap.isEmpty()) {
            return Collections.emptyMap();
        }
        Map<String, Object> retMap = new HashMap<String, Object>(paramMap.size());

        Set<Map.Entry<String, String[]>> entrySet = paramMap.entrySet();

        for (Map.Entry<String, String[]> entry : entrySet) {
            String name = entry.getKey();
            String[] values = entry.getValue();
            if (values.length == 1) {
                retMap.put(name, values[0]);
            } else if (values.length > 1) {
                retMap.put(name, values);
            } else {
                retMap.put(name, "");
            }
        }

        return retMap;
    }

}
