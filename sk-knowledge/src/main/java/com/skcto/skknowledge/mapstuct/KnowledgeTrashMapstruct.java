package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.domain.KnowledgeTrash;
import com.skcto.skknowledge.dto.KnowledgeTrashQueryDTO;
import com.skcto.skknowledge.vo.KnowledgeTrashVo;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface KnowledgeTrashMapstruct extends BasePageMapstruct<KnowledgeTrash, KnowledgeTrashVo, KnowledgeTrashQueryDTO>{
    KnowledgeTrashMapstruct INSTANCE = Mappers.getMapper(KnowledgeTrashMapstruct.class);

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "trashTime", expression = "java(new java.util.Date())")
    KnowledgeTrash docToKnowledgeTrash(KnowledgeDoc knowledgeDoc);

    @Mapping(target= "vectorStatus", constant = "1")
    KnowledgeDoc knowledgeTrashToDoc(KnowledgeTrash knowledgeTrash);

}
