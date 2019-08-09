package com.zyd.sop.adminserver.api.service;

import com.alibaba.fastjson.JSON;
import com.gitee.easyopen.annotation.Api;
import com.gitee.easyopen.annotation.ApiService;
import com.gitee.easyopen.doc.annotation.ApiDoc;
import com.gitee.easyopen.doc.annotation.ApiDocMethod;
import com.gitee.easyopen.util.CopyUtil;
import com.gitee.fastmybatis.core.query.Query;
import com.gitee.fastmybatis.core.query.Sort;
import com.gitee.fastmybatis.core.support.PageEasyui;
import com.gitee.fastmybatis.core.util.MapperUtil;
import com.zyd.sop.adminserver.api.service.param.ConfigIpBlackForm;
import com.zyd.sop.adminserver.api.service.param.ConfigIpBlacklistPageParam;
import com.zyd.sop.adminserver.api.service.result.ConfigIpBlacklistVO;
import com.zyd.sop.adminserver.bean.ChannelMsg;
import com.zyd.sop.adminserver.bean.ZookeeperContext;
import com.zyd.sop.adminserver.common.BizException;
import com.zyd.sop.adminserver.entity.ConfigIpBlacklist;
import com.zyd.sop.adminserver.mapper.ConfigIpBlacklistMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * @author tanghc
 */
@ApiService
@ApiDoc("IP黑名单管理")
@Slf4j
public class IPBlacklistApi {

    @Autowired
    ConfigIpBlacklistMapper configIpBlacklistMapper;

    @ApiDocMethod(description = "获取IP黑名单，分页")
    @Api(name = "ip.blacklist.page")
    PageEasyui<ConfigIpBlacklistVO> page(ConfigIpBlacklistPageParam form) {
        Query query = Query.build(form);
        query.orderby("id", Sort.DESC);
        return MapperUtil.queryForEasyuiDatagrid(configIpBlacklistMapper, query, ConfigIpBlacklistVO.class);
    }

    @ApiDocMethod(description = "IP黑名单--新增")
    @Api(name = "ip.blacklist.add")
    void add(ConfigIpBlackForm form) {
        ConfigIpBlacklist rec = configIpBlacklistMapper.getByColumn("ip", form.getIp());
        if (rec != null) {
            throw new BizException("IP已添加");
        }
        rec = new ConfigIpBlacklist();
        CopyUtil.copyPropertiesIgnoreNull(form, rec);
        configIpBlacklistMapper.saveIgnoreNull(rec);
        try {
            this.sendIpBlacklistMsg(rec, BlacklistMsgType.ADD);
        } catch (Exception e) {
            log.error("推送IP黑名单失败, rec:{}",rec, e);
            throw new BizException("推送IP黑名单失败");
        }
    }

    @ApiDocMethod(description = "IP黑名单--修改")
    @Api(name = "ip.blacklist.update")
    void update(ConfigIpBlackForm form) {
        ConfigIpBlacklist rec = configIpBlacklistMapper.getById(form.getId());
        CopyUtil.copyPropertiesIgnoreNull(form, rec);
        configIpBlacklistMapper.updateIgnoreNull(rec);
    }

    @ApiDocMethod(description = "IP黑名单--删除")
    @Api(name = "ip.blacklist.del")
    void del(long id) {
        ConfigIpBlacklist rec = configIpBlacklistMapper.getById(id);
        if (rec == null) {
            return;
        }
        configIpBlacklistMapper.deleteById(id);
        try {
            this.sendIpBlacklistMsg(rec, BlacklistMsgType.DELETE);
        } catch (Exception e) {
            log.error("推送IP黑名单失败, rec:{}",rec, e);
            throw new BizException("推送IP黑名单失败");
        }
    }

    public void sendIpBlacklistMsg(ConfigIpBlacklist configIpBlacklist, BlacklistMsgType blacklistMsgType) throws Exception {
        String configData = JSON.toJSONString(configIpBlacklist);
        ChannelMsg channelMsg = new ChannelMsg(blacklistMsgType.name().toLowerCase(), configData);
        String jsonData = JSON.toJSONString(channelMsg);
        String path = ZookeeperContext.getIpBlacklistChannelPath();
        log.info("消息推送--IP黑名单设置({}), path:{}, data:{}",blacklistMsgType.name(), path, jsonData);
        ZookeeperContext.createOrUpdateData(path, jsonData);
    }

    enum BlacklistMsgType {
        /**
         * 黑名单消息类型：添加
         */
        ADD,
        /**
         * 黑名单消息类型：删除
         */
        DELETE
    }
}
