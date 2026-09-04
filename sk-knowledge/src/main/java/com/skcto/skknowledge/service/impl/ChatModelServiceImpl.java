package com.skcto.skknowledge.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.dto.ChatModelDTO;
import com.skcto.skknowledge.mapstuct.ChatModelMapstruct;
import com.skcto.skknowledge.mapper.ChatModelMapper;
import com.skcto.skknowledge.mapstuct.MyChatModelMapStruct;
import com.skcto.skknowledge.page.PageQuery;
import com.skcto.skknowledge.result.DataPageInfo;
import com.skcto.skknowledge.service.ChatModelService;
import com.skcto.skknowledge.util.LoginInfoUtil;
import com.skcto.skknowledge.vo.ChatModelVo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Date;
import java.util.List;


@RequiredArgsConstructor
@Service
public class ChatModelServiceImpl extends ServiceImpl<ChatModelMapper, ChatModel>
    implements ChatModelService{


    private final ChatModelMapstruct chatModelMapstruct;

    private final ChatModelMapper chatModelMapper;

    private final MyChatModelMapStruct myChatModelMapStruct;


    @Override
    public boolean insertByDTO(ChatModelDTO chatModelDTO) {

//        ChatModel chatModel = new ChatModel();
//        BeanUtils.copyProperties(chatModelDTO, chatModel);

//        ChatModel chatModel = myChatModelMapStruct.dtoToEntity(chatModelDTO);
        ChatModel chatModel = chatModelMapstruct.dtoToEntity(chatModelDTO);

        return save(chatModel);
    }

    @Override
    public DataPageInfo<ChatModelVo> selectModelByPage(PageQuery pageQuery) {

        Page<ChatModel> pageChatModel = page(new Page<>(pageQuery.getPageNum(), pageQuery.getPageSize()));

        //将page转成DataPageInfo
//        DataPageInfo<ChatModel> chatModelDataPageInfo = new DataPageInfo<>(
//                pageChatModel.getTotal(),
//                pageChatModel.getSize(),
//                pageChatModel.getCurrent(),
//                pageChatModel.getRecords()
//        );

        DataPageInfo<ChatModelVo> chatModelDataPageInfo = chatModelMapstruct.entityToDataPageInfo(pageChatModel);

        return chatModelDataPageInfo;
    }

    @Override
    public ChatModelVo getVoById(Integer id) {
        ChatModel chatModel = getById(id);
        return chatModelMapstruct.entityToVo(chatModel);
    }

    @Override
    public boolean updateByDTO(ChatModelDTO chatModelDTO) {
        ChatModel updateChatModel = chatModelMapstruct.dtoToEntity(chatModelDTO);


        //判断是否修改了ApiKey
        if (updateChatModel.getApiKey().contains("****")) {
            //如果程序进入，则说明没有修改Apikey
            ChatModel chatModel = getById(updateChatModel.getId());
            updateChatModel.setApiKey(chatModel.getApiKey());
        }

        return updateById(updateChatModel);
    }

    @Override
    public List<ChatModelVo> selectModelByDTO(ChatModelDTO chatModelDTO) {
        //根据传入的dto条件查询
        LambdaQueryWrapper<ChatModel> queryWrapper = new LambdaQueryWrapper<>();

        //判断type是否有值
        if (StringUtils.hasText(chatModelDTO.getType())) {
            queryWrapper.eq(ChatModel::getType, chatModelDTO.getType());
        }

        List<ChatModel> chatModels = chatModelMapper.selectList(queryWrapper);

        return chatModelMapstruct.entityToVo(chatModels);
    }
}
