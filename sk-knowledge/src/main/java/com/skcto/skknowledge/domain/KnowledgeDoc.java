package com.skcto.skknowledge.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 知识库附件
 * @TableName knowledge_doc
 */
@TableName(value ="knowledge_doc")
@Data
public class KnowledgeDoc extends BaseEntity {


    /**
     * 知识库ID
     */
    private Integer knowledgeId;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 文档大小 字节
     */
    private Long docSize;

    /**
     * 对象存储地址
     */
    private String url;

    /**
     * 写入向量数据库状态1未开始，2进行中，3已完成
     */
    private Integer vectorStatus;

}