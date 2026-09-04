package com.skcto.skknowledge.mapstuct;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.skcto.skknowledge.result.DataPageInfo;

import java.util.Collections;
import java.util.List;


public interface BasePageMapstruct<E, V,D> {
    V entityToVo(E entity);
    List<V> entityToVo(List<E> entities);
    E dtoToEntity(D dto);
    List<E> dtoToEntity(List<D> dtos);
    List<V> dtoToVo(List<D> dtos);
    V dtoToVo(D dto);

    /**
     * 将传入的page信息转成DataPageInfo
     */
    default DataPageInfo<V> entityToDataPageInfo(IPage<E> page){
        //获取数据库中查询的数据信息
        List<E> content = page.getRecords();

        //将实体转成vo集合
        List<V> voList = entityToVo(content);

        return new DataPageInfo<>(
                page.getTotal(),
                page.getSize(),
                page.getCurrent(),
                voList
        );

    }

    default DataPageInfo<V> dtoToDataPageInfo(IPage<D> page){
        List<D> content = page.getRecords();

        List<V> voList = dtoToVo(content);

        return new DataPageInfo<>(
                page.getTotal(),
                page.getSize(),
                page.getCurrent(),
                voList
        );
    }
}