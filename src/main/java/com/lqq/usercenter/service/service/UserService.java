package com.lqq.usercenter.service.service;

import com.lqq.usercenter.model.domain.User;
import com.baomidou.mybatisplus.extension.service.IService;

import javax.servlet.http.HttpServletRequest;

/**
* @author li
* @description 针对表【user(用户表)】的数据库操作Service
* @createDate 2023-09-20 21:27:35
*/
public interface UserService extends IService<User> {

    /**
     * 用户注册
     *
     * @param account 用户账号
     * @param password 用户密码
     * @param checkPassword 校验码
     * @return
     */
    long userRegister(String account,String password ,String checkPassword);

    /**
     * 用户登录
     *
     * @param account  用户账号
     * @param password 用户密码
     * @param request
     * @return
     */
    User userLogin(String account, String password, HttpServletRequest request);

    /**
     * 用户脱敏
     * @param origin
     * @return
     */
    User getSafetyUser(User origin);
}
