package com.lqq.usercenter.model.domain.request;

import lombok.Data;

import java.io.Serializable;

@Data
public class UserRegisterRequest implements Serializable {


    private static final long serialVersionUID = -4126436627993982851L;
    private String userAccount;
    private String userPassword;
    private String checkPassword;

}
