package com.zyd.sop.gatewaycommon.result;

import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author tanghc
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class JsonResult extends ApiResult {
    private Object data;
}
