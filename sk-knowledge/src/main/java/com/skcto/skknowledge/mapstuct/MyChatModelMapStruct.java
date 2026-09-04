package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.dto.ChatModelDTO;
import com.skcto.skknowledge.vo.ChatModelVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;

import java.util.List;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface MyChatModelMapStruct extends MyBasePageMapStruct<ChatModel, ChatModelVo, ChatModelDTO> {

}