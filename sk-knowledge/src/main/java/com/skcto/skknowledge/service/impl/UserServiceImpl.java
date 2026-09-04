package com.skcto.skknowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skcto.skknowledge.constant.ResultCodeEnum;
import com.skcto.skknowledge.domain.Permission;
import com.skcto.skknowledge.domain.User;
import com.skcto.skknowledge.mapper.PermissionMapper;
import com.skcto.skknowledge.service.UserService;
import com.skcto.skknowledge.mapper.UserMapper;
import jakarta.annotation.Resource;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
public class UserServiceImpl extends ServiceImpl<UserMapper, User>
    implements UserService, UserDetailsService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private PermissionMapper permissionMapper;

    /**
     * 该方法在spring security框架登录的时候被调用
     *
     * @param username
     * @return
     * @throws UsernameNotFoundException
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        //查询数据库，查询页面上传过来的这个用户名是否在数据库中存在，也就是根据该username查询用户对象

        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, username);
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new UsernameNotFoundException(ResultCodeEnum.ACCOUNT_ERROR.getMessage());
        }

        //查询该用户的权限code列表（一个用户可能有多个权限code）
        List<Permission> permissionList = permissionMapper.selectByUserId(user.getId());
        //把查询出来的角色放入用户对象中
        user.setPermissionList(permissionList);

        //返回该用户对象
        return user;
    }
}




