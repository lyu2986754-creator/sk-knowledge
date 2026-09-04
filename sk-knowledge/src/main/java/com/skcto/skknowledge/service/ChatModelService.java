package com.skcto.skknowledge.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.dto.ChatModelDTO;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.vo.ChatModelVo;

import java.util.List;


public interface ChatModelService extends IService<ChatModel> {

    boolean insertByDTO(ChatModelDTO chatModelDTO);

    DataPageInfo<ChatModelVo> selectModelByPage(PageQuery pageQuery);

    ChatModelVo getVoById(Integer id);

    boolean updateByDTO(ChatModelDTO chatModelDTO);

    List<ChatModelVo> selectModelByDTO(ChatModelDTO chatModelDTO);
}
