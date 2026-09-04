package com.skcto.skknowledge.mapper;

import com.skcto.skknowledge.domain.Permission;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import java.util.List;


public interface PermissionMapper extends BaseMapper<Permission> {

    List<Permission> selectByUserId(Integer id);
}




