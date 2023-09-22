package com.lqq.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lqq.usercenter.model.domain.User;
import com.lqq.usercenter.model.domain.request.UserLoginRequest;
import com.lqq.usercenter.model.domain.request.UserRegisterRequest;
import com.lqq.usercenter.service.service.UserService;

import org.apache.commons.lang3.StringUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static com.lqq.usercenter.contant.UserConstant.ADMIN_ROLE;
import static com.lqq.usercenter.contant.UserConstant.USER_LOGIN_STATE;

@RestController
@RequestMapping("/user")
public class UserController {




    //因为要调用业务逻辑，所以要引入service层
    @Resource
    private UserService userService;

    @PostMapping("/register")//@RequestBody使前端传来的参数和UserRegisterRequest做关联
    public Long userRegister(@RequestBody UserRegisterRequest userRegisterRequest){//封装一个request专门接受请求

        //如果请求参数为空，直接返回为空
        if(userRegisterRequest == null){
            return null;
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();

        //如果获得参数为空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return null;
        }

        return userService.userRegister(userAccount, userPassword, checkPassword);
    }

    @PostMapping("/login")//@RequestBody使前端传来的参数和UserRegisterRequest做关联
    public User userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){//封装一个request专门接受请求

        //如果请求参数为空，直接返回为空
        if(userLoginRequest == null){
            return null;
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        //如果获得参数为空
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            return null;
        }

        return userService.userLogin(userAccount, userPassword, request);
    }

    /**
     * 搜索用户
     * @param username
     * @return
     */
    @GetMapping("/search")
    public List<User> usersSearch(@RequestBody String username,HttpServletRequest request){
        if(!isAdmin(request)){//如果不是管理员
            return new ArrayList<>();
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        return userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList()); //返回查询到的用户

    }

    /**
     * 删除用户
     * @param userId
     * @return
     */
    @PostMapping("/delete")
    public boolean usersDelete(@RequestBody long userId,HttpServletRequest request){

        if(!isAdmin(request)){//如果不是管理员
            return false;
        }
       if(userId <= 0){
           return false;
       }
        return userService.removeById(userId);//返回查询到的用户
    }

    private boolean isAdmin(@RequestBody HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User)userObj;
        if(user == null || user.getUserRole() != ADMIN_ROLE){//判断是否为管理员
            return false;
        }
        return true;
    }
}
