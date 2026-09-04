package com.skcto.skknowledge.mapstuct;

import com.skcto.skknowledge.domain.ChatPrompt;
import com.skcto.skknowledge.vo.ChatPromptVo;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatPromptMapstruct {


    ChatPromptVo entityToVo(ChatPrompt chatPrompt);

    List<ChatPromptVo> entityToVo(List<ChatPrompt> chatPrompt);

}