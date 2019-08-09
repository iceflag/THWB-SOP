package com.zyd.sop.servercommon.param;

import com.alibaba.fastjson.JSONObject;
import com.zyd.sop.servercommon.annotation.ApiAbility;
import com.zyd.sop.servercommon.annotation.ApiMapping;
import com.zyd.sop.servercommon.bean.OpenContext;
import com.zyd.sop.servercommon.bean.OpenContextImpl;
import com.zyd.sop.servercommon.bean.ServiceContext;
import com.zyd.sop.servercommon.util.OpenUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.util.ReflectionUtils;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 解析request参数中的业务参数，绑定到方法参数上
 *
 * @author tanghc
 */
@Slf4j
public class ApiArgumentResolver implements SopHandlerMethodArgumentResolver {

    private final Map<MethodParameter, HandlerMethodArgumentResolver> argumentResolverCache = new ConcurrentHashMap<>(256);

    private ParamValidator paramValidator = new ServiceParamValidator();

    private RequestMappingHandlerAdapter requestMappingHandlerAdapter;

    @Override
    public void setRequestMappingHandlerAdapter(RequestMappingHandlerAdapter requestMappingHandlerAdapter) {
        this.requestMappingHandlerAdapter = requestMappingHandlerAdapter;
    }

    @Override
    public boolean supportsParameter(MethodParameter methodParameter) {
        boolean hasAnnotation = methodParameter.getMethodAnnotation(ApiMapping.class) != null
                || methodParameter.getMethodAnnotation(ApiAbility.class) != null;
        // 有注解
        return hasAnnotation;
    }

    @Override
    public Object resolveArgument(MethodParameter methodParameter, ModelAndViewContainer modelAndViewContainer, NativeWebRequest nativeWebRequest, WebDataBinderFactory webDataBinderFactory) throws Exception {
        Object paramObj = this.getParamObject(methodParameter, nativeWebRequest);
        if (paramObj != null) {
            // JSR-303验证
            paramValidator.validateBizParam(paramObj);
            return paramObj;
        } else {
            HandlerMethodArgumentResolver resolver = getOtherArgumentResolver(methodParameter);
            if (resolver != null) {
                return resolver.resolveArgument(methodParameter, modelAndViewContainer, nativeWebRequest, webDataBinderFactory);
            }
            return null;
        }
    }


    /**
     * 获取参数对象，将request中的参数绑定到实体对象中去
     *
     * @param methodParameter
     * @param nativeWebRequest
     * @return 没有返回null
     */
    protected Object getParamObject(MethodParameter methodParameter, NativeWebRequest nativeWebRequest) {
        HttpServletRequest request = (HttpServletRequest) nativeWebRequest.getNativeRequest();
        JSONObject requestParams = OpenUtil.getRequestParams(request);
        Class<?> parameterType = methodParameter.getParameterType();
        // 方法参数类型
        Class<?> bizObjClass = parameterType;
        boolean isOpenRequestParam = parameterType == OpenContext.class;
        // 参数是OpenRequest，则取OpenRequest的泛型参数类型
        if (isOpenRequestParam) {
            bizObjClass = this.getOpenRequestGenericParameterClass(methodParameter);
        }
        OpenContext openContext = new OpenContextImpl(requestParams, bizObjClass);
        ServiceContext.getCurrentContext().setOpenContext(openContext);
        Object bizObj = openContext.getBizObject();
        this.bindUploadFile(bizObj, nativeWebRequest);
        return isOpenRequestParam ? openContext : bizObj;
    }

    /**
     * 获取泛型参数类型
     * @param methodParameter 参数
     * @return 返回泛型参数class
     */
    protected Class<?> getOpenRequestGenericParameterClass(MethodParameter methodParameter) {
        Type genericParameterType = methodParameter.getGenericParameterType();
        Class<?> bizObjClass = null;
        if (genericParameterType instanceof ParameterizedType) {
            Type[] params = ((ParameterizedType) genericParameterType).getActualTypeArguments();
            if (params != null && params.length >= 1) {
                bizObjClass = (Class<?>) params[0];
            }
        }
        return bizObjClass;
    }

    /**
     * 将上传文件对象绑定到属性中
     *
     * @param bizObj           业务参数
     * @param nativeWebRequest
     */
    protected void bindUploadFile(Object bizObj, NativeWebRequest nativeWebRequest) {
        if (bizObj == null) {
            return;
        }
        if (this.isMultipartRequest(nativeWebRequest)) {
            Object nativeRequest = nativeWebRequest.getNativeRequest();
            MultipartHttpServletRequest request = (MultipartHttpServletRequest) nativeRequest;
            Class<?> bizClass = bizObj.getClass();
            ReflectionUtils.doWithFields(bizClass, field -> {
                ReflectionUtils.makeAccessible(field);
                String name = field.getName();
                MultipartFile multipartFile = request.getFile(name);
                ReflectionUtils.setField(field, bizObj, multipartFile);
            }, field -> field.getType() == MultipartFile.class);
        }
    }

    protected boolean isMultipartRequest(NativeWebRequest nativeWebRequest) {
        return nativeWebRequest.getNativeRequest() instanceof MultipartHttpServletRequest;
    }

    /**
     * 获取其它的参数解析器
     *
     * @param parameter 业务参数
     * @return 返回合适参数解析器，没有返回null
     */
    protected HandlerMethodArgumentResolver getOtherArgumentResolver(MethodParameter parameter) {
        HandlerMethodArgumentResolver result = this.argumentResolverCache.get(parameter);
        if (result == null) {
            List<HandlerMethodArgumentResolver> argumentResolvers = this.requestMappingHandlerAdapter.getArgumentResolvers();
            for (HandlerMethodArgumentResolver methodArgumentResolver : argumentResolvers) {
                if (methodArgumentResolver instanceof SopHandlerMethodArgumentResolver) {
                    continue;
                }
                if (methodArgumentResolver.supportsParameter(parameter)) {
                    result = methodArgumentResolver;
                    this.argumentResolverCache.put(parameter, result);
                    break;
                }
            }
        }
        return result;
    }

    public void setParamValidator(ParamValidator paramValidator) {
        this.paramValidator = paramValidator;
    }
}
