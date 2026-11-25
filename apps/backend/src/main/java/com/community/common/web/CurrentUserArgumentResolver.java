package com.community.common.web;

import com.community.common.error.BusinessException;
import com.community.common.error.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

/**
 * 把 {@link CurrentUser} 标注的参数解析成 {@link CurrentUserInfo}。
 *
 * <p>游客访问可选登录接口时得到 {@code null}；标注 {@code required = true} 时直接抛出
 * 20001，由统一异常处理返回 401。</p>
 */
public class CurrentUserArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        return parameter.hasParameterAnnotation(CurrentUser.class)
                && CurrentUserInfo.class.isAssignableFrom(parameter.getParameterType());
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
        HttpServletRequest request = webRequest.getNativeRequest(HttpServletRequest.class);
        Object userId = request == null ? null : request.getAttribute(AuthAttributes.CURRENT_USER_ID);

        CurrentUser annotation = parameter.getParameterAnnotation(CurrentUser.class);
        if (userId == null) {
            if (annotation != null && annotation.required()) {
                throw new BusinessException(ErrorCode.UNAUTHORIZED);
            }
            return null;
        }
        return new CurrentUserInfo((Long) userId);
    }
}
