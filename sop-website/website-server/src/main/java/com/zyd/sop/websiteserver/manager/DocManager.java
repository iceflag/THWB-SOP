package com.zyd.sop.websiteserver.manager;

import com.zyd.sop.websiteserver.bean.DocInfo;
import com.zyd.sop.websiteserver.bean.DocItem;

import java.util.Collection;

/**
 * @author tanghc
 */
public interface DocManager {

    void load(String serviceId);

    DocItem get(String method, String version);

    DocInfo getByTitle(String title);

    Collection<DocInfo> listAll();
}
