package com.zyd.sop.adminserver.api.service;

import com.gitee.easyopen.annotation.Api;
import com.gitee.easyopen.annotation.ApiService;
import com.gitee.easyopen.doc.annotation.ApiDoc;
import com.gitee.easyopen.doc.annotation.ApiDocMethod;
import com.gitee.easyopen.util.CopyUtil;
import com.gitee.fastmybatis.core.query.Query;
import com.zyd.sop.adminserver.api.service.param.LimitParam;
import com.zyd.sop.adminserver.api.service.param.RouteSearchParam;
import com.zyd.sop.adminserver.api.service.result.LimitVO;
import com.zyd.sop.adminserver.bean.GatewayRouteDefinition;
import com.zyd.sop.adminserver.bean.RouteConfigDto;
import com.zyd.sop.adminserver.common.BizException;
import com.zyd.sop.adminserver.common.LimitEnum;
import com.zyd.sop.adminserver.entity.ConfigRouteLimit;
import com.zyd.sop.adminserver.mapper.ConfigRouteLimitMapper;
import com.zyd.sop.adminserver.service.RouteConfigService;
import com.zyd.sop.adminserver.service.RouteService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.CollectionUtils;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * 限流
 *
 * @author tanghc
 */
@ApiService
@ApiDoc("服务管理")
@Slf4j
public class LimitApi {

    @Autowired
    RouteService routeService;

    @Autowired
    RouteConfigService routeConfigService;

    @Autowired
    ConfigRouteLimitMapper configRouteLimitMapper;

    @Api(name = "route.limit.list")
    @ApiDocMethod(description = "限流列表", elementClass = LimitVO.class)
    List<LimitVO> listLimit(RouteSearchParam param) throws Exception {
        List<GatewayRouteDefinition> routeDefinitionList = routeService.getRouteDefinitionList(param);
        if (CollectionUtils.isEmpty(routeDefinitionList)) {
            return Collections.emptyList();
        }
        List<String> routeIdList = getRouteIdList(routeDefinitionList);
        // key：routeId
        Map<String, ConfigRouteLimit> routeLimitMap = getStoreConfigRouteLimit(routeIdList);
        List<LimitVO> gatewayRouteDefinitions = routeDefinitionList
                .stream()
                .map(gatewayRouteDefinition -> {
                    String routeId = gatewayRouteDefinition.getId();
                    LimitVO vo = new LimitVO();
                    CopyUtil.copyPropertiesIgnoreNull(gatewayRouteDefinition, vo);
                    ConfigRouteLimit configRouteLimit = routeLimitMap.getOrDefault(routeId, getDefaultLimit());
                    CopyUtil.copyPropertiesIgnoreNull(configRouteLimit, vo);
                    vo.setRouteId(routeId);
                    vo.setHasRecord(BooleanUtils.toInteger(configRouteLimit.getId() != null));
                    return vo;
                })
                .collect(Collectors.toList());
        return gatewayRouteDefinitions;
    }

    private List<String> getRouteIdList(List<GatewayRouteDefinition> routeDefinitionList) {
        return routeDefinitionList
                .stream()
                .map(GatewayRouteDefinition::getId)
                .collect(Collectors.toList());
    }

    private Map<String, ConfigRouteLimit> getStoreConfigRouteLimit(List<String> routeIdList) {
        Query query = new Query();
        query.in("route_id", routeIdList);
        // key：routeId
        Map<String, ConfigRouteLimit> routeLimitMap = configRouteLimitMapper.list(query)
                .stream()
                .collect(Collectors.toMap(ConfigRouteLimit::getRouteId, Function.identity()));
        return routeLimitMap;
    }

    private ConfigRouteLimit getDefaultLimit() {
        ConfigRouteLimit configRouteLimit = new ConfigRouteLimit();
        configRouteLimit.setLimitType(LimitEnum.TYPE_LEAKY_BUCKET.getVal());
        configRouteLimit.setLimitStatus(LimitEnum.STATUS_CLOSE.getVal());
        return configRouteLimit;
    }

    @Api(name = "route.limit.update")
    @ApiDocMethod(description = "修改限流")
    @Transactional(rollbackFor = Exception.class)
    public void updateLimtit(LimitParam param) {
        String routeId = param.getRouteId();
        ConfigRouteLimit configRouteLimit = configRouteLimitMapper.getByColumn("route_id", routeId);
        if (configRouteLimit == null) {
            configRouteLimit = new ConfigRouteLimit();
            CopyUtil.copyPropertiesIgnoreNull(param, configRouteLimit);
            configRouteLimitMapper.save(configRouteLimit);
        } else {
            CopyUtil.copyPropertiesIgnoreNull(param, configRouteLimit);
            configRouteLimitMapper.updateIgnoreNull(configRouteLimit);
        }
        RouteConfigDto routeConfigDto = new RouteConfigDto();
        CopyUtil.copyPropertiesIgnoreNull(param, routeConfigDto);
        try {
            routeConfigService.sendRouteConfigMsg(routeConfigDto);
        } catch (Exception e) {
            log.error("推送限流消息错误, param:{}", param, e);
            throw new BizException("修改失败，请查看日志");
        }
    }
}
