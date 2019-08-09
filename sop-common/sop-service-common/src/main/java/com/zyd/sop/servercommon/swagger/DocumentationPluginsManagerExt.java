package com.zyd.sop.servercommon.swagger;

import com.zyd.sop.servercommon.annotation.ApiAbility;
import com.zyd.sop.servercommon.annotation.ApiMapping;
import com.zyd.sop.servercommon.bean.ServiceConfig;
import com.zyd.sop.servercommon.mapping.RouteUtil;
import com.google.common.base.Optional;
import springfox.documentation.service.Operation;
import springfox.documentation.service.StringVendorExtension;
import springfox.documentation.service.VendorExtension;
import springfox.documentation.spi.service.contexts.OperationContext;
import springfox.documentation.spring.web.plugins.DocumentationPluginsManager;

import java.util.List;

/**
 * @author tanghc
 */
public class DocumentationPluginsManagerExt extends DocumentationPluginsManager {

    public static final String SOP_NAME = "sop_name";
    public static final String SOP_VERSION = "sop_version";

    @Override
    public Operation operation(OperationContext operationContext) {
        Operation operation = super.operation(operationContext);
        this.setVendorExtension(operation, operationContext);
        return operation;
    }

    private void setVendorExtension(Operation operation, OperationContext operationContext) {
        List<VendorExtension> vendorExtensions = operation.getVendorExtensions();
        Optional<ApiMapping> mappingOptional = operationContext.findAnnotation(ApiMapping.class);
        if (mappingOptional.isPresent()) {
            ApiMapping apiMapping = mappingOptional.get();
            String name = apiMapping.value()[0];
            String version = buildVersion(apiMapping.version());
            vendorExtensions.add(new StringVendorExtension(SOP_NAME, name));
            vendorExtensions.add(new StringVendorExtension(SOP_VERSION, version));
        } else {
            Optional<ApiAbility> abilityOptional = operationContext.findAnnotation(ApiAbility.class);
            if (abilityOptional.isPresent()) {
                ApiAbility apiAbility = abilityOptional.get();
                String mappingPattern = operationContext.requestMappingPattern();
                String name = RouteUtil.buildApiName(mappingPattern);
                String version = buildVersion(apiAbility.version());
                vendorExtensions.add(new StringVendorExtension(SOP_NAME, name));
                vendorExtensions.add(new StringVendorExtension(SOP_VERSION, version));
            }
        }
    }

    private String buildVersion(String version) {
        if ("".equals(version)) {
            return ServiceConfig.getInstance().getDefaultVersion();
        } else {
            return version;
        }
    }
}
