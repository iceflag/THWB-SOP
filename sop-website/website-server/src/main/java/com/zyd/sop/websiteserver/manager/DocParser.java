package com.zyd.sop.websiteserver.manager;

import com.alibaba.fastjson.JSONObject;
import com.zyd.sop.websiteserver.bean.DocInfo;

/**
 * @author tanghc
 */
public interface DocParser {
    DocInfo parseJson(JSONObject docRoot);
}
