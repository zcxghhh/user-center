package com.lqq.usercenter.service.service;
import java.util.Date;

import com.lqq.usercenter.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;

import static org.junit.jupiter.api.Assertions.*;

/**
 * 用户服务测试
 *
 * @author li
 */
@SpringBootTest
class UserServiceTest {

    @Resource
    private UserService userService;

    @Test
    void testAddUser(){
        User user = new User();
        user.setUsername("lqq");
        user.setUserAccount("123");
        user.setGender(0);
        user.setUserPassword("xxx");
        user.setAvatarUrl("");
        user.setPhone("456");
        user.setEmail("123");


        boolean res = userService.save(user);//添加数据
        System.out.println(user.getId());
        Assertions.assertTrue(res);//断言，期望res返回true

    }

    @Test
    void userRegister() {
        String username = "zcxg";
        String userAccount = "zcxg";
        String userPassword = "123456789";
        String checkPassword = "123456789";
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,result);
        userAccount = "lqq";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,result);
        userAccount = "12345";
        userPassword = "123456";
        result = userService.userRegister(userAccount, userPassword, checkPassword);
        Assertions.assertEquals(-1,result);

    }
}