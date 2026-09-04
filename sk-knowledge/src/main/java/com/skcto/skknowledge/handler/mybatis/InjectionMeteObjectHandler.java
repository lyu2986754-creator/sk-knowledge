package com.skcto.skknowledge.handler.mybatis;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import com.skcto.skknowledge.domain.BaseEntity;
import com.skcto.skknowledge.util.LoginInfoUtil;
import org.apache.ibatis.reflection.MetaObject;

import java.util.Date;
import java.util.Objects;

/**
 * 元对象
 */
public class InjectionMeteObjectHandler implements MetaObjectHandler {
    @Override
    public void insertFill(MetaObject metaObject) {
        //判断metaObject是否为BaseEntity
        if (metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            Date date = Objects.isNull(baseEntity.getCreateTime()) ? new Date():baseEntity.getCreateTime();
            baseEntity.setCreateTime(date);

            if(Objects.isNull(baseEntity.getCreateBy())){
                baseEntity.setCreateBy(LoginInfoUtil.getCurrentLoginUser().getId());
            }
        }else{
            //实体类没有继承BaseEntity
            Date date = new Date();
            strictInsertFill(metaObject, "createTime", Date.class, date);
        }
    }

    @Override
    public void updateFill(MetaObject metaObject) {
        //判断metaObject是否为BaseEntity
        if (metaObject.getOriginalObject() instanceof BaseEntity baseEntity) {
            Date date = Objects.isNull(baseEntity.getUpdateTime()) ? new Date():baseEntity.getUpdateTime();
            baseEntity.setUpdateTime(date);

            if(Objects.isNull(baseEntity.getUpdateBy())){
                baseEntity.setUpdateBy(LoginInfoUtil.getCurrentLoginUser().getId());
            }
        }else{
            //实体类没有继承BaseEntity
            Date date = new Date();
            strictUpdateFill(metaObject, "UpdateTime", Date.class, date);
        }
    }
}
