package com.example.blogapi.handler;

import com.alibaba.fastjson.JSON;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.LoginService;
import com.example.blogapi.utils.JWTUtils;
import com.example.blogapi.utils.UserThreadLocal;
import com.example.blogapi.vo.ErrorCode;
import com.example.blogapi.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@Component
@Slf4j
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private LoginService loginService;
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        //在执行controller方法之前进行执行
        /*
         * 1.需要判断请求的接口路径是否为HandlerMethod(controller方法)
         * 2.判断token是否为空，如果为空，未登录
         * 3.如果token不为空，登陆验证loginService checkToken
         * 4.如果认证成功，放行
         */
        if (!(handler instanceof HandlerMethod)){
            //handler可能是RequestResourceHandler
            return true;
        }

        String token = request.getHeader("Authorization");

        log.info("=================request start===========================");
        String requestURI = request.getRequestURI();
        log.info("request uri:{}",requestURI);
        log.info("request method:{}",request.getMethod());
        log.info("token:{}", token);
        log.info("=================request end===========================");

        if (token == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        SysUser sysUser = loginService.checkToken(token);
        if (sysUser == null){
            Result result = Result.fail(ErrorCode.NO_LOGIN.getCode(), "未登录");
            response.setContentType("application/json;charset=utf-8");
            response.getWriter().print(JSON.toJSONString(result));
            return false;
        }
        UserThreadLocal.put(sysUser);
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //如果不删除ThreadLocal中用完的信息，会有内存泄露的风险
        UserThreadLocal.remove();
    }
}
