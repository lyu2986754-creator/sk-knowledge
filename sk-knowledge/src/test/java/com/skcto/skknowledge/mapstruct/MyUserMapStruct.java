package com.skcto.skknowledge.mapstruct;

import com.skcto.skknowledge.eneity.User;
import com.skcto.skknowledge.eneity.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

/**
 * Mapstruct会生成该接口的实现类，实现类对象会交给spring容器管理
 */
@Mapper(componentModel = "spring")
public interface MyUserMapStruct {

    /**
     * 解决了两个类中属性不一致的问题
     */
    @Mapping(source = "userId", target = "id")
    User dtoDomain(UserDTO userDTO);
}
