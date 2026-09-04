package com.skcto.skknowledge.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatCompletionsVo {
    private String id;
    //聊天窗口id
    private String windowId;
    //知识库id
    private Integer knowledgeId;
    //根据提问生成的标题
    private String tittle;
    //模型id
    private Integer modelId;
    //内容
    private String content;
    //角色 user/assistant
    private String role;
    //创建时间
    private Date createTime;
}
