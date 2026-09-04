package com.skcto.skknowledge.vo;

import lombok.Data;

import java.util.Date;

/**
 * 知识库附件
 */
@Data
public class KnowledgeDocVo {

    private Integer id;

    /**
     * 知识库ID
     */
    private Integer knowledgeId;


    /**
     * 知识库名称
     */
    private String knowledgeName;

    /**
     * 文档名称
     */
    private String docName;

    /**
     * 文档类型
     */
    private String docType;

    /**
     * 文件访问
     */
    private String url;

    /**
     * 文档大小 字节
     */
    private Long docSize;

    /**
     * 写入向量数据库状态1未开始，2进行中，3已完成
     */
    private Integer vectorStatus;

    private Date createTime;
}