package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.dto.ChatModelDTO;
import com.skcto.skknowledge.vo.ChatModelVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface ChatModelMapstruct extends BasePageMapstruct<ChatModel, ChatModelVo, ChatModelDTO> {
    ChatModelMapstruct INSTANCE = Mappers.getMapper(ChatModelMapstruct.class);

}