package com.skcto.skknowledge.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skcto.skknowledge.domain.KnowledgeBase;
import com.skcto.skknowledge.dto.KnowledgeBaseDTO;
import com.skcto.skknowledge.mapper.KnowledgeBaseMapper;
import com.skcto.skknowledge.mapstuct.KnowledgeBaseMapstruct;
import com.skcto.skknowledge.service.KnowledgeBaseService;
import com.skcto.skknowledge.vo.KnowledgeBaseVo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class KnowledgeBaseServiceImpl extends ServiceImpl<KnowledgeBaseMapper, KnowledgeBase>
    implements KnowledgeBaseService{

    private final KnowledgeBaseMapstruct knowledgeBaseMapstruct;


    @Override
    public boolean insertByDTO(KnowledgeBaseDTO knowledgeBaseDTO) {
        KnowledgeBase knowledgeBase = knowledgeBaseMapstruct.dtoToEntity(knowledgeBaseDTO);

        return save(knowledgeBase);
    }

    @Override
    public List<KnowledgeBaseVo> selectBaseVo() {
        List<KnowledgeBase> list = list();

        return knowledgeBaseMapstruct.entityToVo(list);
    }
}




