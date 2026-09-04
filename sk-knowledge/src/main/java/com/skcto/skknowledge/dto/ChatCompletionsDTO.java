package com.skcto.skknowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
@Builder
public class ChatCompletionsDTO {

    private String id;
    //聊天窗口id
    private String windowId;
    //聊天窗口标题
    private String tittle;
    //知识库id
    private Integer knowledgeId;
    //模型id
    private Integer modelId;
    //内容
    private String content;
    //角色 user/assistant
    private String role;

    //登录用户id
    private Integer loginUserId;
    //创建时间
    @DateTimeFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

}
