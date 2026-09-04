package com.skcto.skknowledge.service;

import com.skcto.skknowledge.domain.KnowledgeBase;
import com.baomidou.mybatisplus.extension.service.IService;
import com.skcto.skknowledge.dto.KnowledgeBaseDTO;
import com.skcto.skknowledge.vo.KnowledgeBaseVo;

import java.util.List;


public interface KnowledgeBaseService extends IService<KnowledgeBase> {


    boolean insertByDTO(KnowledgeBaseDTO knowledgeBaseDTO);

    List<KnowledgeBaseVo> selectBaseVo();
}
