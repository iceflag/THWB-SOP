package com.zyd.sop.websiteserver.bean;

import lombok.Data;

import java.util.List;

/**
 * @author tanghc
 */
@Data
public class DocInfo {
    private String title;
    private List<DocModule> docModuleList;
}
