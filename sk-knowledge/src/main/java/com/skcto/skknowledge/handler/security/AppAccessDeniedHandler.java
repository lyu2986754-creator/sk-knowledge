package com.skcto.skknowledge.handler.security;

import cn.hutool.json.JSONUtil;
import com.skcto.skknowledge.constant.ResultCodeEnum;
import com.skcto.skknowledge.result.Result;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppAccessDeniedHandler implements AccessDeniedHandler {

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException {
        //返回json，告诉前端: 权限不足
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        //采用构建器模式，链式编程创建一个R对象
        Result result = Result.builder().code(ResultCodeEnum.PERMISSION.getCode()).msg(ResultCodeEnum.PERMISSION.getMessage()).build();

        //hutool工具包，把R对象转成json字符串
        String json = JSONUtil.toJsonStr(result);

        //把json写出去，写出到浏览器客户端
        response.getWriter().write(json);
    }
}
