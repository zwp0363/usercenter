package com.zwp.usercenter.common;

import lombok.Data;

import java.io.Serializable;

/**
 * 通用返回类
 * @param <T>
 */
@Data
public class BaseResponse<T> implements Serializable {

    private int code;

    private T data;

    private String message;

    private String description;

    // 当你需要创建(new)一个 BaseResponse 对象，并且想要明确设置状态码、数据、消息和描述时，就应该使用这个构造方法
    public BaseResponse(int code, T data, String message, String description) {
        this.code = code;
        this.data = data;
        this.message = message;
        this.description = description;
    }

    // 当你需要创建一个 BaseResponse 对象，只需要设置状态码和数据，不需要或者暂时不需要提供详细的消息和描述时，可以使用这个构造方法
    public BaseResponse(int code, T data) {
        this(code, data, "", "");
    }

    public BaseResponse(int code, T data, String message) {
        this(code, data, message, "");
    }

    public BaseResponse(ErrorCode errorCode) {
        this(errorCode.getCode(), null, errorCode.getMessage(), errorCode.getDescription());
    }
}
