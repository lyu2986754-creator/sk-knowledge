package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.dto.ChatModelDTO;
import com.skcto.skknowledge.vo.ChatModelVo;

import java.util.List;

public interface MyBasePageMapStruct<E,V,D> {
    D entityToDto(E entity);

    V entityToVo(E entity);

    List<V> entityToVo(List<E> entities);
}
