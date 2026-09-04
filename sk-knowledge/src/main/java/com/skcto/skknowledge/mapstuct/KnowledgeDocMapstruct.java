package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.dto.KnowledgeDocQueryDTO;
import com.skcto.skknowledge.vo.KnowledgeDocVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface KnowledgeDocMapstruct extends BasePageMapstruct<KnowledgeDoc, KnowledgeDocVo, KnowledgeDocQueryDTO> {

    KnowledgeDocMapstruct INSTANCE = Mappers.getMapper(KnowledgeDocMapstruct.class);
}