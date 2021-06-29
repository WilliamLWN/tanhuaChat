package com.tanhua.server.interceptor;

import com.tanhua.domain.db.User;
import com.tanhua.server.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class TokenInterceptor implements HandlerInterceptor {

    @Autowired
    private UserService userService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        log.info("执行拦截器的preHandle()方法，统一身份认证");

        //1. 从Authorization请求头中获取token信息
        String token = request.getHeader("Authorization");

        //2. 判断，如果请求没有携带token，说明非法用户，认证失败，401
        if(token == null) {
            response.setStatus(401);
            return false;
        }

        //3. 根据token获取用户
        User user = userService.findUserByToken(token);
        if (user == null) {
            response.setStatus(401);
            return false;
        }

        //4. 用户绑定到本地线程对象上
        UserHolder.setUser(user);

        return true;
    }
}
