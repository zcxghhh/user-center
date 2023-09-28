package com.lqq.usercenter.service.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.lqq.usercenter.common.ErrorCode;
import com.lqq.usercenter.exception.BusinessException;
import com.lqq.usercenter.service.service.UserService;
import com.lqq.usercenter.model.domain.User;
import com.lqq.usercenter.mapper.UserMapper;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.lqq.usercenter.contant.UserConstant.USER_LOGIN_STATE;


/**
* @author li
* @description 针对表【user(用户表)】的数据库操作Service实现
* @createDate 2023-09-20 21:27:35
*/
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService {

    @Resource
    private UserMapper userMapper;

    /**
     * 盐值,混淆密码
     */
    private static final String SALT = "lqq";

    /**
     * 用户注册
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验码
     * @param planetCode  星球编号
     * @return 用户
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword,String planetCode) {

        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword,planetCode)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"参数信息为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户账号长度过短，请输入长度大于等于的账号");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户密码长度过短，请输入长度大于等于的密码");
        }

        if(planetCode.length() > 5){//限制长度不能超过5
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户星球编号长度过长，请输入长度小于等于五位数的星球编号");
        }
        //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }
        if(!userPassword.equals(checkPassword)){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码不等于校验码");
        }

        //账号不能重复,把这个查询数据库的操作放到后面，如果账号包含特殊字符，就省去了查询数据库的操作
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号已存在");
        }
        //把校验账号和星球编号拆开，可以知道是为什么返回
        //星球编号不能重复,把这个查询数据库的操作放到后面，如果账号包含特殊字符，就省去了查询数据库的操作
        queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("planetCode",planetCode);
        count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"星球编号已存在");
        }

        //2. 加盐  非对称加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+ userPassword).getBytes());
        System.out.println(encryptPassword);

        //插入数据，操作数据库
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        //将数据插到数据库中
        boolean saveRes = this.save(user);
        if(!saveRes){
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        return user.getId();
    }


    /**
     * 用户登录
     *
     * @param userAccount  用户账号
     * @param userPassword 用户密码
     * @param request 接收参数
     * @return 用户
     */
    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {

        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword)){
            throw new BusinessException(ErrorCode.NULL_ERROR,"请求数据为空");
        }
        if(userAccount.length() < 4){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号过短");
        }
        if (userPassword.length() < 8 ){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"密码过长");
        }

        //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"账号包含特殊字符");
        }

        //2. 加盐  非对称加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        //查询用户是否存在 查看用户账号和密码是否一致---->到数据库中查询数据，看用户账号是否存在
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //如果用户为空
        if(user == null){
            log.info("user login failed,userAccount can not match userPassword");
            throw new BusinessException(ErrorCode.PARAMS_ERROR,"用户不存在");
        }

        //3.用户脱敏
        User safetyUser = getSafetyUser(user);

        //4. 记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE,user);
        return safetyUser; //返回脱敏的用户信息，避免用户信息泄露

    }

    /**
     * 用户脱敏
     * @param origin
     * @return
     */
    @Override
    public User getSafetyUser(User origin){

        User safetyUser = new User();
        safetyUser.setId(origin.getId());
        safetyUser.setUsername(origin.getUsername());
        safetyUser.setUserAccount(origin.getUserAccount());
        safetyUser.setGender(origin.getGender());
        safetyUser.setPlanetCode(origin.getPlanetCode());
        safetyUser.setAvatarUrl(origin.getAvatarUrl());
        safetyUser.setPhone(origin.getPhone());
        safetyUser.setEmail(origin.getEmail());
        safetyUser.setUserRole(origin.getUserRole());
        safetyUser.setUserStatus(origin.getUserStatus());
        safetyUser.setCreateTime(origin.getCreateTime());

        return safetyUser;
    }

    /**
     * 用户注销
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        request.getSession().removeAttribute(USER_LOGIN_STATE);//从session移除登录态
        return 1;
    }
}




