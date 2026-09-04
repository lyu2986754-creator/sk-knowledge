package com.skcto.skknowledge.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 知识库回收站
 * @TableName knowledge_trash
 */
@TableName(value ="knowledge_trash")
@Data
public class KnowledgeTrash extends BaseEntity{

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
     * 文档大小 单位 字节
     */
    private Long docSize;

    /**
     * 对象存储地址
     */
    private String url;

    /**
     * 放入回收站时间
     */
    private Date trashTime;



}