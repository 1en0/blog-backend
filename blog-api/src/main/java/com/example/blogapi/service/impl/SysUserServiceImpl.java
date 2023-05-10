package com.example.blogapi.service.impl;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.StringUtils;
import com.example.blogapi.dao.mapper.SysUserMapper;
import com.example.blogapi.dao.pojo.SysUser;
import com.example.blogapi.service.SysUserService;
import com.example.blogapi.utils.JWTUtils;
import com.example.blogapi.vo.ErrorCode;
import com.example.blogapi.vo.LoginUserVo;
import com.example.blogapi.vo.Result;
import com.example.blogapi.vo.UserVo;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class SysUserServiceImpl implements SysUserService {
    @Autowired
    private SysUserMapper sysUserMapper;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;
    @Override
    public SysUser findUserById(Long id) {
        SysUser sysUser = sysUserMapper.selectById(id);
        if(sysUser == null){
            sysUser = new SysUser();
            sysUser.setNickname("匿名");
        }

        return sysUser;
    }

    @Override
    public SysUser findUser(String account, String password) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.eq(SysUser::getPassword,password);
        queryWrapper.select(SysUser::getId,SysUser::getAccount,SysUser::getAvatar,SysUser::getNickname);
        queryWrapper.last("limit 1");
        SysUser sysUser = sysUserMapper.selectOne(queryWrapper);
        return sysUser;
    }

    @Override
    public Result getUserInfoByToken(String token) {
        /**
         * 1.token合法性校验
         *   是否为空，解析是否成功，redis是否存在
         * 2.如果校验失败，返回错误
         * 3.如果成功，返回对应的结果 LoginUserVo
         */
        Map<String, Object> map = JWTUtils.checkToken(token);
        if (map == null){
            return Result.fail(ErrorCode.NO_LOGIN.getCode(),ErrorCode.NO_LOGIN.getMsg());
        }
        String userJson = redisTemplate.opsForValue().get("TOKEN_" + token);
        if (StringUtils.isBlank(userJson)){
            return Result.fail(ErrorCode.NO_LOGIN.getCode(),ErrorCode.NO_LOGIN.getMsg());
        }
        System.out.println(userJson);
        SysUser sysUser = JSON.parseObject(userJson, SysUser.class);
        LoginUserVo loginUserVo = new LoginUserVo();
        loginUserVo.setAccount(sysUser.getAccount());
        loginUserVo.setAvatar(sysUser.getAvatar());
        loginUserVo.setId(String.valueOf(sysUser.getId()));
        loginUserVo.setNickname(sysUser.getNickname());
        return Result.success(loginUserVo);
    }

    @Override
    public SysUser findUserByAccount(String account) {
        LambdaQueryWrapper<SysUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(SysUser::getAccount,account);
        queryWrapper.last("limit 1");
        return sysUserMapper.selectOne(queryWrapper);
    }

    @Override
    public void save(SysUser sysUser) {
        //注意 默认生成的id 是分布式id 采用了雪花算法
        this.sysUserMapper.insert(sysUser);
    }

    @Override
    public UserVo findUserVoById(Long id) {
        SysUser sysUser = findUserById(id);
        UserVo userVo = new UserVo();
        if(sysUser == null){
            sysUser = new SysUser();
            sysUser.setId(1L);
            sysUser.setAvatar("/static/img/logo.b3a48c0.png");
            sysUser.setNickname("匿名");
        }
        BeanUtils.copyProperties(sysUser, userVo);
        userVo.setId(String.valueOf(sysUser.getId()));
        return userVo;
    }
}
