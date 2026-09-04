package com.skcto.skknowledge.handler.security;

import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;

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
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 登录成功，会执行该handler
 */
@Component
public class AppAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    //直接注入该RedisTemplate就可以操作redis了，这个类的泛型分别是放入redis中的key的类型和value的类型
    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        //生成jwt (token)
        User user = (User) authentication.getPrincipal();
        String userJSON = JSONUtil.toJsonStr(user);
        String token = JWTUtil.createToken(Map.of("user", userJSON), Constant.SECRET.getBytes());


        //存储token到redis中，并设置过期时间
        redisTemplate.opsForValue().set(Constant.REDIS_TOKEN_KEY + user.getId(), token, 1L, TimeUnit.DAYS);

        //采用构建器模式，链式编程创建一个Result对象
        Result result = Result.builder().code(ResultCodeEnum.SUCCESS.getCode()).msg(ResultCodeEnum.SUCCESS.getMessage()).data(token).build();

        //转成json字符串
        String json = JSONUtil.toJsonStr(result);

        //把json写出去，写出到浏览器客户端
        response.getWriter().write(json);
    }
}
