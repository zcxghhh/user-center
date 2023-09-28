package com.lqq.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 * @author Li
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;//因为在业务层，返回的数据类型·不一致，所以用泛型，提高通用型

    private String message;

    private String description;

    public BaseResponse(int code, T data, String message ,String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;

    }
    public BaseResponse(int code, String message ,String description) {
        this.code = code;
        this.message = message;
        this.description = description;

    }

    public BaseResponse(int code, T data) {
        this.code = code;
        this.data = data;
        this.message = "";
        this.description = "";
    }
    public BaseResponse(int code, T data,String message) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = "";
    }

    /**
     * 失败的时候怎么处理
     * @param errorCode
     */
    public BaseResponse(ErrorCode errorCode){
        //异常时数据返回空
        this(errorCode.getCode(),null, errorCode.getMessage(),errorCode.getDescription());
    }

    public BaseResponse(ErrorCode errorCode,String message,String description){
        //异常时数据返回空
        this(errorCode.getCode(),null, message,description);
    }

    public BaseResponse(ErrorCode errorCode,String description){
        //异常时数据返回空
        this(errorCode.getCode(),null, errorCode.getMessage(),description);
    }

}
