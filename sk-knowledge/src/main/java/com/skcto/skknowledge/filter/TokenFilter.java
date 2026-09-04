package com.skcto.skknowledge.filter;


import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import cn.hutool.jwt.JWTUtil;
import com.skcto.skknowledge.constant.Constant;
import com.skcto.skknowledge.constant.ResultCodeEnum;
import com.skcto.skknowledge.domain.User;
import com.skcto.skknowledge.result.Result;
import com.skcto.skknowledge.util.JsonUtil;
import jakarta.annotation.Resource;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class TokenFilter extends OncePerRequestFilter {

    @Resource
    private RedisTemplate redisTemplate;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        response.setContentType("application/json");
        response.setCharacterEncoding("utf-8");

        //登录接口，不需要验证token（因为登录时，还没有生成token）
        String requestUri = request.getRequestURI();

        //如果是登录请求和建立sse连接，我们不需要验证token
        if (requestUri.equals("/api/login") || requestUri.startsWith("/api/chat/stream")) {
            //直接放行，不需要验证token
            filterChain.doFilter(request, response);
        } else {
            String token = request.getHeader("Authorization"); //从请求头中获取token的值
            if (!StringUtils.hasText(token)) { //前面有个 “非”
                Result result = Result.builder().code(ResultCodeEnum.ARGUMENT_VALID_ERROR.getCode()).msg(ResultCodeEnum.ARGUMENT_VALID_ERROR.getMessage()).build();
                response.getWriter().write(JsonUtil.toJsonString(result));
            } else {
                boolean verify = false; //验证的初始值是false，false表示验证未通过
                try {
                    token = token.replace("Bearer ", "");
                    //验证通过了，则verify = true
                    verify = JWTUtil.verify(token, Constant.SECRET.getBytes());
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (!verify) { //前面有个 “非”
                    Result result = Result.builder().code(ResultCodeEnum.ILLEGAL_REQUEST.getCode()).msg(ResultCodeEnum.ILLEGAL_REQUEST.getMessage()).build();
                    response.getWriter().write(JSONUtil.toJsonStr(result));
                } else {
                    JSONObject payloads = JWTUtil.parseToken(token).getPayloads();
                    String userJSON = payloads.get("user", String.class);
                    User user = JSONUtil.toBean(userJSON, User.class);
                    Integer userId = user.getId();
                    //拿redis的token

                    String redisToken = (String) redisTemplate.opsForValue().get(Constant.REDIS_TOKEN_KEY + userId);
                    if (!token.equals(redisToken)) { //前面有个 “非”
                        Result result = Result.builder().code(ResultCodeEnum.TOKEN_EXPIRED.getCode()).msg(ResultCodeEnum.TOKEN_EXPIRED.getMessage()).build();
                        response.getWriter().write(JSONUtil.toJsonStr(result));
                    } else {
                        //token验证通过了
                        UsernamePasswordAuthenticationToken authenticationToken
                                = new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
                        SecurityContextHolder.getContext().setAuthentication(authenticationToken);

                        //放行
                        filterChain.doFilter(request, response);
                    }
                }
            }
        }
    }
}
