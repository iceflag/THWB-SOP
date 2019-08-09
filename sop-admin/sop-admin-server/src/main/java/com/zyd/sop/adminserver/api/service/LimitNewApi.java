package com.zyd.sop.adminserver.api.service;

import com.gitee.easyopen.annotation.Api;
import com.gitee.easyopen.annotation.ApiService;
import com.gitee.easyopen.doc.annotation.ApiDoc;
import com.gitee.easyopen.doc.annotation.ApiDocMethod;
import com.gitee.easyopen.util.CopyUtil;
import com.gitee.fastmybatis.core.PageInfo;
import com.gitee.fastmybatis.core.query.Query;
import com.gitee.fastmybatis.core.query.Sort;
import com.gitee.fastmybatis.core.util.MapperUtil;
import com.zyd.sop.adminserver.api.service.param.LimitNewAddParam;
import com.zyd.sop.adminserver.api.service.param.LimitNewParam;
import com.zyd.sop.adminserver.api.service.param.LimitNewUpdateParam;
import com.zyd.sop.adminserver.api.service.result.LimitNewVO;
import com.zyd.sop.adminserver.bean.ConfigLimitDto;
import com.zyd.sop.adminserver.common.BizException;
import com.zyd.sop.adminserver.entity.ConfigLimit;
import com.zyd.sop.adminserver.mapper.ConfigLimitMapper;
import com.zyd.sop.adminserver.service.RouteConfigService;
import com.zyd.sop.adminserver.service.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/**
 * 限流
 *
 * @author tanghc
 */
@ApiService
@ApiDoc("服务管理-限流管理")
@Slf4j
public class LimitNewApi {

    @Autowired
    RouteService routeService;

    @Autowired
    RouteConfigService routeConfigService;

    @Autowired
    ConfigLimitMapper configLimitMapper;

    @Api(name = "config.limit.list")
    @ApiDocMethod(description = "限流列表(新)", elementClass = LimitNewVO.class)
    PageInfo<ConfigLimit> listLimit(LimitNewParam param) throws Exception {
        Query query = Query.build(param);
        query.orderby("route_id", Sort.ASC)
                .orderby("app_key", Sort.ASC)
                .orderby("order_index", Sort.ASC);
        PageInfo<ConfigLimit> pageInfo = MapperUtil.query(configLimitMapper, query);
        return pageInfo;
    }

    @Api(name = "config.limit.add")
    @ApiDocMethod(description = "新增限流(新)")
    @Transactional(rollbackFor = Exception.class)
    public void createLimtit(LimitNewAddParam param) {
        ConfigLimit configLimit = new ConfigLimit();
        CopyUtil.copyPropertiesIgnoreNull(param, configLimit);
        configLimitMapper.save(configLimit);
        ConfigLimitDto configLimitDto = buildConfigLimitDto(configLimit);
        try {
            routeConfigService.sendLimitConfigMsg(configLimitDto);
        } catch (Exception e) {
            log.error("推送限流消息错误, param:{}", param, e);
            throw new BizException("新增失败，请查看日志");
        }
    }

    @Api(name = "config.limit.update")
    @ApiDocMethod(description = "修改限流(新)")
    @Transactional(rollbackFor = Exception.class)
    public void updateLimtit(LimitNewUpdateParam param) {
        ConfigLimit configLimit = configLimitMapper.getById(param.getId());
        if (configLimit == null) {
            configLimit = new ConfigLimit();
            CopyUtil.copyPropertiesIgnoreNull(param, configLimit);
            configLimitMapper.save(configLimit);
        } else {
            CopyUtil.copyPropertiesIgnoreNull(param, configLimit);
            configLimitMapper.update(configLimit);
        }
        ConfigLimitDto configLimitDto = buildConfigLimitDto(configLimit);
        try {
            routeConfigService.sendLimitConfigMsg(configLimitDto);
        } catch (Exception e) {
            log.error("推送限流消息错误, param:{}", param, e);
            throw new BizException("修改失败，请查看日志");
        }
    }

    private ConfigLimitDto buildConfigLimitDto(ConfigLimit configLimit) {
        ConfigLimitDto configLimitDto = new ConfigLimitDto();
        CopyUtil.copyPropertiesIgnoreNull(configLimit, configLimitDto);
        return configLimitDto;
    }
}
