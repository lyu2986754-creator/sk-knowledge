package com.skcto.skknowledge.vo;

import lombok.Data;

/**
 * 知识库
 */
@Data
public class KnowledgeBaseVo {

    private Integer id;

    /**
     * 知识库名称
     */
    private String knowledgeName;

    /**
     * 是否公开知识库（0 否 1是）
     */
    private Integer share;

    /**
     * 描述
     */
    private String description;

    /**
     * 知识分隔符
     */
    private String knowledgeSeparator;

    /**
     * 提问分隔符
     */
    private String questionSeparator;

    /**
     * 重叠字符数
     */
    private Integer overlapChar;

    /**
     * 知识库中检索的条数
     */
    private Integer retrieveLimit;

    /**
     * 文本块大小
     */
    private Integer textBlockSize;

    /**
     * 向量库
     */
    private Integer vectorModelId;

    /**
     * 向量模型
     */
    private Integer embeddingModelId;

    /**
     * 系统提示词
     */
    private String systemPrompt;



}