package com.example.blogapi.service;

import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.params.LoginParam;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public interface LoginService {
    /**
     * 登录功能
     * @param loginParam
     * @return
     */
    Result login(LoginParam loginParam);

    Result logout(String token);

    /**
     * 注册
     * @param loginParam
     * @return
     */
    Result register(LoginParam loginParam);

    SysUser checkToken(String token);
}
