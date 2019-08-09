package com.zyd.sop.adminserver.bean;

import lombok.Data;

/**
 * @author tanghc
 */
@Data
public class ChannelMsg {

    public ChannelMsg(String operation, Object data) {
        this.operation = operation;
        this.data = data;
    }

    private String operation;
    private Object data;
}
