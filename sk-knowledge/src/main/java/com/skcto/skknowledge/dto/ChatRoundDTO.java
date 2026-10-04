package com.skcto.skknowledge.dto;

import lombok.Data;

/**
 * 一轮已经完成的外部问答。
 *
 * 用途：Agentic 编排服务自己完成检索与生成后，把这一轮结果交回本服务落库。
 * 为什么由本服务落库而不是让调用方直接写 MongoDB：
 *   会话数据的形状（窗口创建、标题、消息结构）只应有一处定义，
 *   否则两个写入方迟早会写出不一致的数据。
 */
@Data
public class ChatRoundDTO {

    // 会话窗口 id；为空表示新建会话
    private String windowId;

    private Integer knowledgeId;

    private Integer modelId;

    // 用户提问
    private String question;

    // 已经生成好的回答
    private String answer;
}

