package com.zyd.sop.adminserver;

import com.zyd.sop.adminserver.api.isv.result.IsvDetailDTO;
import com.zyd.sop.adminserver.mapper.IsvInfoMapper;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author tanghc
 */
public class IsvInfoMapperTest extends SopAdminServerApplicationTests {

    @Autowired
    IsvInfoMapper isvInfoMapper;

    @Test
    public void testGet() {
        IsvDetailDTO isvDetail = isvInfoMapper.getIsvDetail("2019032617262200001");
        System.out.println(isvDetail);
    }
}
