package com.skcto.skknowledge.handler.mvc;

import com.skcto.skknowledge.result.Result;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 处理全局异常
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 出现异常之后会执行到该方法中
     * @param e
     * @return
     */
    @ExceptionHandler(Exception.class)
    public Result handleException(Exception e){
        e.printStackTrace();
        return Result.FAIL(e.getMessage());
    }

}