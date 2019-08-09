package com.zyd.sop.websiteserver.controller;

import com.zyd.sop.websiteserver.bean.DocInfo;
import com.zyd.sop.websiteserver.bean.DocItem;
import com.zyd.sop.websiteserver.manager.DocManager;
import com.zyd.sop.websiteserver.vo.DocBaseInfoVO;
import com.zyd.sop.websiteserver.vo.DocInfoVO;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

/**
 * @author tanghc
 */
@RestController
@RequestMapping("doc")
public class DocController {

    @Autowired
    DocManager docManager;

    @Value("${api.url-test}")
    String urlTest;

    @Value("${api.url-prod}")
    String urlProd;

    @Value("${api.pwd}")
    String pwd;

    @GetMapping("/getDocBaseInfo")
    public DocBaseInfoVO getDocBaseInfo() {
        List<DocInfoVO> docInfoList = docManager.listAll()
                .stream()
                .map(docInfo -> {
                    DocInfoVO vo = new DocInfoVO();
                    BeanUtils.copyProperties(docInfo, vo);
                    return vo;
                })
                .collect(Collectors.toList());

        DocBaseInfoVO baseInfoVO = new DocBaseInfoVO();
        baseInfoVO.setUrlTest(urlTest);
        baseInfoVO.setUrlProd(urlProd);
        baseInfoVO.setDocInfoList(docInfoList);
        return baseInfoVO;
    }

    @GetMapping("/docinfo/{title}")
    public DocInfo getDocModule(@PathVariable("title") String title) {
        return docManager.getByTitle(title);
    }

    @GetMapping("/item/{method}/{version}/")
    public DocItem getDocItem(@PathVariable("method") String method, @PathVariable("version") String version) {
        return docManager.get(method, version);
    }


    // 后门地址，可手动更新文档内容，一般情况下用不到
    @GetMapping("/reload")
    public String reload(String pwd) {
        boolean correct = StringUtils.equals(this.pwd, pwd);
        if (correct) {
            docManager.load(null);
        }
        return String.valueOf(correct);
    }
}
