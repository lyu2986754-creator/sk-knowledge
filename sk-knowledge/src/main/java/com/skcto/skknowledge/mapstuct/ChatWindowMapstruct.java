package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.ChatWindow;
import com.skcto.skknowledge.dto.ChatWindowDTO;
import com.skcto.skknowledge.vo.ChatWindowVo;
import org.mapstruct.Mapper;
import org.mapstruct.MappingInheritanceStrategy;
import org.mapstruct.factory.Mappers;

@Mapper(componentModel = "spring",mappingInheritanceStrategy = MappingInheritanceStrategy.AUTO_INHERIT_FROM_CONFIG)
public interface ChatWindowMapstruct extends BasePageMapstruct<ChatWindow, ChatWindowVo, ChatWindowDTO> {
    ChatWindowMapstruct INSTANCE = Mappers.getMapper(ChatWindowMapstruct.class);

}