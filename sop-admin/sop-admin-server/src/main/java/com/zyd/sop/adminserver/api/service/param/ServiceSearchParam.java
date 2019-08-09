package com.zyd.sop.adminserver.api.service.param;

import com.gitee.easyopen.doc.annotation.ApiDocField;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

/**
 * @author tanghc
 */
@Getter
@Setter
public class ServiceSearchParam {

    @ApiDocField(description = "服务名serviceId")
    private String serviceId;
}
