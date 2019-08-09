package com.zyd.sop.servercommon.manager;

import com.zyd.sop.servercommon.bean.ServiceApiInfo;

/**
 * @author tanghc
 */
public interface ApiMetaManager {
    /**
     * 上传API
     * @param serviceApiInfo
     */
    void uploadApi(ServiceApiInfo serviceApiInfo);
}
