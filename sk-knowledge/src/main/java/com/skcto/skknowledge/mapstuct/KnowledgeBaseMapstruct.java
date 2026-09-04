package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.KnowledgeBase;
import com.skcto.skknowledge.dto.KnowledgeBaseDTO;
import com.skcto.skknowledge.vo.KnowledgeBaseVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface KnowledgeBaseMapstruct extends BasePageMapstruct<KnowledgeBase, KnowledgeBaseVo, KnowledgeBaseDTO> {

    KnowledgeBaseMapstruct INSTANCE = Mappers.getMapper(KnowledgeBaseMapstruct.class);
}