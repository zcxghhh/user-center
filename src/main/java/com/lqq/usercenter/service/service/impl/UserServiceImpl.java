package com.lqq.usercenter.service.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
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
     *
     * @param userAccount 用户账号
     * @param userPassword 用户密码
     * @param checkPassword 校验码
     * @return 用户
     */
    @Override
    public long userRegister(String userAccount, String userPassword, String checkPassword) {

        //1.校验
        if(StringUtils.isAnyBlank(userAccount,userPassword,checkPassword)){
            return -1;
        }
        if(userAccount.length() < 4){
            return -1;
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8){
            return -1;
        }

        //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return -1;
        }
        if(!userPassword.equals(checkPassword)){
            return -1;
        }

        //账号不能重复,把这个查询数据库的操作放到后面，如果账号包含特殊字符，就省去了查询数据库的操作
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if(count > 0){
            return -1;
        }

        //2. 加盐  非对称加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+ userPassword).getBytes());
        System.out.println(encryptPassword);
        //插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
        boolean saveRes = this.save(user);
        if(!saveRes){
            return -1;
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
            return null;
        }
        if(userAccount.length() < 4){
            return null;
        }
        if (userPassword.length() < 8 ){
            return null;
        }

        //账号不能包含特殊字符
        String validPattern = "[`~!@#$%^&*()+=|{}':;',\\[\\].<>/?~！@#￥%……&*（）——+|{}【】‘；：”“’。，、？]";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if(matcher.find()){
            return null;
        }

        //2. 加盐  非对称加密
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT+ userPassword).getBytes());
        //查询用户是否存在 查看用户账号和密码是否一致
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount",userAccount);
        queryWrapper.eq("userPassword",encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        //如果用户为空
        if(user == null){
            log.info("user login failed,userAccount can not macth userPassword");
            return null;
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
        safetyUser.setAvatarUrl(origin.getAvatarUrl());
        safetyUser.setPhone(origin.getPhone());
        safetyUser.setEmail(origin.getEmail());
        safetyUser.setUserRole(origin.getUserRole());
        safetyUser.setUserStatus(origin.getUserStatus());
        safetyUser.setCreateTime(origin.getCreateTime());

        return safetyUser;
    }


}




