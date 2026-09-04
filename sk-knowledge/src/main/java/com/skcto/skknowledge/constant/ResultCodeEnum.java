package com.skcto.skknowledge.constant;

import lombok.Getter;

/**
 * 统一返回结果状态信息类
 *
 */
@Getter
public enum ResultCodeEnum {

    SUCCESS(200,"成功"),
    FAIL(201, "失败"),
    SERVICE_ERROR(500, "服务异常"),
    DATA_ERROR(203, "数据异常"),
    ILLEGAL_REQUEST(204, "非法请求"),
    REPEAT_SUBMIT(205, "重复提交"),
    FEIGN_FAIL(206, "远程调用失败"),
    UPDATE_ERROR(207, "数据更新失败"),
    LOGIN_AUTH(208, "未登陆"),
    PERMISSION(403, "没有权限"),
    ARGUMENT_VALID_ERROR(210, "参数校验异常"),
    ACCOUNT_ERROR(211, "账号不正确"),
    PASSWORD_ERROR(212, "密码不正确"),
    TOKEN_EXPIRED(401, "token过期"),
    ;



    private Integer code;

    private String message;

    ResultCodeEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
