package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.ChatCompletions;
import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.dto.ChatCompletionsDTO;
import com.skcto.skknowledge.dto.ChatModelDTO;
import com.skcto.skknowledge.vo.ChatCompletionsVo;
import com.skcto.skknowledge.vo.ChatModelVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface ChatCompletionsMapstruct extends BasePageMapstruct<ChatCompletions, ChatCompletionsVo, ChatCompletionsDTO> {
    ChatCompletionsMapstruct INSTANCE = Mappers.getMapper(ChatCompletionsMapstruct.class);

}