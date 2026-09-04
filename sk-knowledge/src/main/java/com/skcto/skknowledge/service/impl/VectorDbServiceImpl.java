package com.skcto.skknowledge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skcto.skknowledge.domain.VectorDb;
import com.skcto.skknowledge.mapper.VectorDbMapper;
import com.skcto.skknowledge.mapstuct.VectorDbMapstruct;
import com.skcto.skknowledge.service.VectorDbService;
import com.skcto.skknowledge.vo.VectorDbVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class VectorDbServiceImpl extends ServiceImpl<VectorDbMapper, VectorDb>
    implements VectorDbService{

    private final VectorDbMapstruct vectorDbMapstruct;


    @Override
    public List<VectorDbVo> queryVectorDbVo() {
        List<VectorDb> list =  list();

        return vectorDbMapstruct.entityToVo(list);
    }
}




