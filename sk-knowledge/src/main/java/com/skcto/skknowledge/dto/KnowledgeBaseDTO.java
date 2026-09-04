package com.skcto.skknowledge.dto;

import lombok.Data;

/**
 * 知识库
 * @TableName knowledge_base
 */
@Data
public class KnowledgeBaseDTO {

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
     * 向量库id
     */
    private Integer vectorModelId;

    /**
     * 向量模型id
     */
    private Integer embeddingModelId;

    /**
     * 系统提示词
     */
    private String systemPrompt;




}