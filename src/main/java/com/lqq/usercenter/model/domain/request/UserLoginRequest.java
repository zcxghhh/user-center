package com.lqq.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserLoginRequest implements Serializable {


    private static final long serialVersionUID = -4483979615443212526L;

    private String userAccount;
    private String userPassword;

}
