package com.zyd.sop.adminserver.api.service.result;

import com.zyd.sop.adminserver.api.isv.result.RoleVO;
import com.zyd.sop.adminserver.bean.GatewayRouteDefinition;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author tanghc
 */
@Getter
@Setter
public class RouteVO extends GatewayRouteDefinition {
    private List<RoleVO> roles;
}
