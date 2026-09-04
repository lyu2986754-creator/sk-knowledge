package com.skcto.skknowledge.handler.security;

import cn.hutool.json.JSONUtil;
import com.skcto.skknowledge.constant.Constant;
import com.skcto.skknowledge.constant.ResultCodeEnum;
import com.skcto.skknowledge.domain.User;
import com.skcto.skknowledge.result.Result;
import jakarta.annotation.Resource;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.logout.LogoutSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class AppLogoutSuccessHandler implements LogoutSuccessHandler {

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onLogoutSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        User user = (User) authentication.getPrincipal();

        //退出成功，需要把redis中登录的token删除一下
        redisTemplate.delete(Constant.REDIS_TOKEN_KEY + user.getId());
        //采用构建器模式，链式编程创建一个R对象
        Result result = Result.builder().code(ResultCodeEnum.SUCCESS.getCode()).msg("退出成功").data(authentication).build();

        //hutool工具包，把R对象转成json字符串
        String json = JSONUtil.toJsonStr(result);

        //把json写出去，写出到浏览器客户端
        response.getWriter().write(json);
    }
}
