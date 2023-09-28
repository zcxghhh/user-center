package com.lqq.usercenter.exception;

import com.lqq.usercenter.common.ErrorCode;

/**
 * 自定义业务异常
 */

//给原本的异常类扩充了两个字段，可以支持自定义的异常
public class BusinessException extends RuntimeException{

    private final int code;
    private final String description;

    public BusinessException(String message, int code, String description) {
        super(message);
        this.code = code;
        this.description = description;
    }

    public BusinessException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = errorCode.getDescription();
    }

    //还可以自己传入错误描述
    public BusinessException(ErrorCode errorCode, String description) {
        super(errorCode.getMessage());
        this.code = errorCode.getCode();
        this.description = description;
    }

    public int getCode() {
        return code;
    }

    public String getDescription() {
        return description;
    }
}
