package com.zyd.sop.websiteserver.bean;

import lombok.Data;

import java.util.List;

/**
 * @author tanghc
 */
@Data
public class DocModule {
    private String module;
    private List<DocItem> docItems;
}
