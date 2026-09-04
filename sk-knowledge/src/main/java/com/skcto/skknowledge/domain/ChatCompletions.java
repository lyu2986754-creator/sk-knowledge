package com.skcto.skknowledge.domain;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("chat_completions")
@Data
public class ChatCompletions {

    @Id
    private String id;

    //聊天窗口id
    private String windowId;
    //知识库id
    private int knowledgeId;
    //模型id
    private int modelId;
    //内容
    private String content;
    //角色 user/assistant
    private String role;
    //创建时间
    private Date createTime;
}
