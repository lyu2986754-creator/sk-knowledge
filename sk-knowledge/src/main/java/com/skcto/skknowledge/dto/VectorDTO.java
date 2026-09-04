package com.skcto.skknowledge.dto;

import lombok.Data;

import java.util.List;

@Data
public class VectorDTO {

    /**
     * 知识库kid
     */
    private Integer knowledgeId;

    /**
     * 文档id
     */
    private Integer docId;

    /**
     * 知识块id列表
     */
    private List<Integer> fragmentIdList;


    /**
     * 切分文本块列表
     */
    private List<String> chunkList;


    /**
     * 向量库模型名称
     */
    private String vectorModelName;

    /**
     * 向量化模型名称
     */
    private String embeddingModelName;

    /**
     * 请求key
     */
    private String apiKey;

    /**
     * 请求地址
     */
    private String baseUrl;
}
