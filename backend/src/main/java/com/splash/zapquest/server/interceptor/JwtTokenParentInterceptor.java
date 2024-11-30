package com.splash.zapquest.server.interceptor;

import com.splash.zapquest.common.context.BaseContext;
import com.splash.zapquest.common.exception.UserNotLoginException;
import com.splash.zapquest.common.properties.JwtProperties;
import com.splash.zapquest.common.constant.JwtClaimsConstant;
import com.splash.zapquest.common.utils.JwtUtil;
import io.jsonwebtoken.Claims;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * jwt令牌校验的拦截器
 */
@Component
@Slf4j
public class JwtTokenParentInterceptor implements HandlerInterceptor {

    @Autowired
    private JwtProperties jwtProperties;

    /**
     * 校验jwt
     *
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //判断当前拦截到的是Controller的方法还是其他资源
        if (!(handler instanceof HandlerMethod)) {
            //当前拦截到的不是动态方法，直接放行
            return true;
        }

        try {
            //1、从请求头中获取令牌
            String token = JwtUtil.extractToken(request);
            if (token == null || token.isEmpty()) {
                throw new UserNotLoginException("User didn't login");
            }

            //2、校验令牌
            log.info("jwt校验:{}", token);
            Claims claims = JwtUtil.parseJWT(jwtProperties.getSecretKey(), token);
            Long userId = Long.valueOf(claims.get(JwtClaimsConstant.PARENT_ID).toString());
            log.info("当前用户的id：", userId);
            BaseContext.setCurrentUser(JwtClaimsConstant.PARENT_ID, userId);
            //3、通过，放行
            return true;
        } catch (Exception ex) {
            //4、不通过，响应401状态码
            response.setStatus(401);
            return false;
        }
    }
}