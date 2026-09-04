package com.skcto.skknowledge.result;

import com.skcto.skknowledge.constant.ResultCodeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 采用Result类封装后端返回的结果 （不管后端返回什么数据，都用Result封装，这样可以使得后端返回的数据结构是统一的）
 *
 */
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class Result {

    private int code;

    private String msg;

    private Object data;

    public static Result OK() {
        return Result.builder().code(ResultCodeEnum.SUCCESS.getCode()).msg(ResultCodeEnum.SUCCESS.getMessage()).data(null).build();
    }

    public static Result OK(Object info) {
        return Result.builder().code(ResultCodeEnum.SUCCESS.getCode()).msg(ResultCodeEnum.SUCCESS.getMessage()).data(info).build();
    }

    public static Result FAIL() {
        return Result.builder().code(ResultCodeEnum.FAIL.getCode()).msg(ResultCodeEnum.FAIL.getMessage()).data(null).build();
    }

    public static Result FAIL(Object info) {
        return Result.builder().code(ResultCodeEnum.FAIL.getCode()).msg(ResultCodeEnum.FAIL.getMessage()).data(info).build();
    }
}
