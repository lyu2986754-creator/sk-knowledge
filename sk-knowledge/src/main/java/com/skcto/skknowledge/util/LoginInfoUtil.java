package com.skcto.skknowledge.util;

import com.skcto.skknowledge.domain.User;
import org.springframework.security.core.context.SecurityContextHolder;

public class LoginInfoUtil {

    /**
     * 获取当前登录人的信息
     *
     * @return
     */
    public static User getCurrentLoginUser() {
        return (User)SecurityContextHolder.getContext().getAuthentication().getPrincipal();
    }
}
