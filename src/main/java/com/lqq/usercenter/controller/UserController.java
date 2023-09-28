package com.lqq.usercenter.controller;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.lqq.usercenter.common.BaseResponse;
import com.lqq.usercenter.common.ErrorCode;
import com.lqq.usercenter.common.ResultUtils;
import com.lqq.usercenter.exception.BusinessException;
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

    /**
     * 用户注册
     * @param userRegisterRequest
     * @return
     */
    @PostMapping("/register")//@RequestBody使前端传来的参数和UserRegisterRequest做关联
    public BaseResponse<Long> userRegister(@RequestBody UserRegisterRequest userRegisterRequest){//封装一个request专门接受请求

        //如果请求参数为空，直接返回为空
        if(userRegisterRequest == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        String userAccount = userRegisterRequest.getUserAccount();
        String userPassword = userRegisterRequest.getUserPassword();
        String checkPassword = userRegisterRequest.getCheckPassword();
        String planetCode = userRegisterRequest.getPlanetCode();

        //如果获得参数为空
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }

        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
//        return new BaseResponse<Long>(0,result,"ok" );
        return ResultUtils.success(result);
    }

    /**
     * 用户登录
     * @param userLoginRequest
     * @param request
     * @return
     */
    @PostMapping("/login")//@RequestBody使前端传来的参数和UserRegisterRequest做关联
    //封装一个request专门接受请求
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request){

        //如果请求参数为空，直接返回为空
        if(userLoginRequest == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数为空");
        }

        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();

        //如果获得参数为空
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数为空");
        }

        User user = userService.userLogin(userAccount, userPassword, request);
//        return new BaseResponse<User>(0,user,"ok");
        return ResultUtils.success(user);
    }

    /**
     * 用户注销
     * @param request
     */
    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request){
        if(request == null){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求参数为空");
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrent(HttpServletRequest request){
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User)userObj;
        if(currentUser == null){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }
        Long userId = currentUser.getId();
        // TODO 校验用户是否合法
        //根据Id查询用户
        User user = userService.getById(userId);
        //用户脱敏
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * 搜索用户
     * @param username
     * @return
     */
    @GetMapping("/search")
    public BaseResponse<List<User>> usersSearch(@RequestBody String username,HttpServletRequest request){
        if(!isAdmin(request)){//如果不是管理员
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if(StringUtils.isNoneBlank(username)){
            queryWrapper.like("username",username);
        }
        List<User> userList = userService.list(queryWrapper);
        //返回查询到的用户
        List<User> result = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(result);
    }

    /**
     * 删除用户
     * @param userId
     * @return
     */
    @PostMapping("/delete")
    public BaseResponse<Boolean> usersDelete(@RequestBody long userId,HttpServletRequest request){

        if(!isAdmin(request)){//如果不是管理员
            throw new BusinessException(ErrorCode.NO_AUTH,"无权限");
        }
       if(userId <= 0){
           throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
       }
        //删除用户
        boolean result = userService.removeById(userId);
       return ResultUtils.success(result);
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
