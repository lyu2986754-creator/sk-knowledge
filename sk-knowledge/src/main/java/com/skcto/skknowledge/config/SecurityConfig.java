package com.skcto.skknowledge.config;


import com.skcto.skknowledge.filter.TokenFilter;
import com.skcto.skknowledge.handler.security.AppAccessDeniedHandler;
import com.skcto.skknowledge.handler.security.AppAuthenticationFailureHandler;
import com.skcto.skknowledge.handler.security.AppAuthenticationSuccessHandler;
import com.skcto.skknowledge.handler.security.AppLogoutSuccessHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.authentication.logout.LogoutFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@EnableMethodSecurity
@Configuration
@RequiredArgsConstructor
public class SecurityConfig {


    private final AppAuthenticationSuccessHandler appAuthenticationSuccessHandler;


    private final AppAuthenticationFailureHandler appAuthenticationFailureHandler;


    private final AppLogoutSuccessHandler appLogoutSuccessHandler;


    private final TokenFilter tokenFilter;


    private final AppAccessDeniedHandler appAccessDeniedHandler;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * 跨域
     *
     * @return
     */
    @Bean
    public CorsConfigurationSource configurationSource() {
        UrlBasedCorsConfigurationSource urlBasedCorsConfigurationSource = new UrlBasedCorsConfigurationSource();

        //跨域配置
        CorsConfiguration corsConfiguration = new CorsConfiguration();
        corsConfiguration.setAllowedOrigins(Arrays.asList("*")); //允许任何来源，http://localhost:10492/
        corsConfiguration.setAllowedMethods(Arrays.asList("*")); //允许任何请求方法，post、get、put、delete
        corsConfiguration.setAllowedHeaders(Arrays.asList("*")); //允许任何的请求头 (jwt)

        //注册跨域配置
        urlBasedCorsConfigurationSource.registerCorsConfiguration("/**", corsConfiguration); //  /api/user
        return urlBasedCorsConfigurationSource;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity httpSecurity, CorsConfigurationSource configurationSource) throws Exception {

        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);

        return httpSecurity
                //配置我们自己的登录页
                .formLogin((formLogin) -> {
                    formLogin.loginProcessingUrl("/api/login") //登录的账号密码往哪个地址提交
                            .successHandler(appAuthenticationSuccessHandler) //登录成功后执行该handler
                            .failureHandler(appAuthenticationFailureHandler);// 登录失败后执行该handler
                })

                .logout((logout) -> {
                    logout.logoutUrl("/api/logout") //退出请求提交到哪个地址
                            .logoutSuccessHandler(appLogoutSuccessHandler); //退出成功后执行该handler
                })

                //其他后端请求都需要添加认证
                .authorizeHttpRequests((authorizeHttpRequests) -> {
                    authorizeHttpRequests
                            .requestMatchers(
                                    "/swagger-ui/**",
                                    "/v3/api-docs/**",
                                    "/swagger-resources/**",
                                    "/swagger-resources",
                                    "/api/chat/stream/**"
                            ).permitAll()
                            .anyRequest().authenticated();
                })

                .csrf((csrf) -> {
                    csrf.disable();
                })

                .cors((cors) -> { //允许前端跨域访问
                    cors.configurationSource(configurationSource);
                })

                .sessionManagement((sessionManagement) -> {
                    //session创建策略 (无session状态)
                    sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS);
                })
                .addFilterBefore(tokenFilter, UsernamePasswordAuthenticationFilter.class)
                .exceptionHandling((exceptionHandling) -> {
                    exceptionHandling.accessDeniedHandler(appAccessDeniedHandler); //没有权限的时候，执行该handler
                })
                .build();
    }


}