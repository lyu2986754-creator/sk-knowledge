package com.skcto.skknowledge.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class KnowledgeTrashQueryDTO {

    private Integer id;

    /**
     * 知识库id
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
     * 文档大小 字节
     */
    private Long docSize;

    /**
     * 文件访问
     */
    private String url;


    private Date trashTime;
}
