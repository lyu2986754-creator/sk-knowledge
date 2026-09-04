package com.skcto.skknowledge.vo;

import lombok.Data;

import java.util.Date;

/**
 * 知识库
 */
@Data
public class KnowledgeTrashVo{

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


    private Date trashTime;



}