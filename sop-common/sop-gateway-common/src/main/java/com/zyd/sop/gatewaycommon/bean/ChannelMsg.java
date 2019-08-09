package com.zyd.sop.gatewaycommon.bean;

import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class ChannelMsg {
    private String operation = "_unknown_";
    private String data = "{}";
}
