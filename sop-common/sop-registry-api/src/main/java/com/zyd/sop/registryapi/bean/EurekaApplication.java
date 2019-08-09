package com.zyd.sop.registryapi.bean;

import lombok.Data;

import java.util.List;

/**
 * @author tanghc
 */
@Data
public class EurekaApplication {
    private String name;
    private List<EurekaInstance> instance;
}
