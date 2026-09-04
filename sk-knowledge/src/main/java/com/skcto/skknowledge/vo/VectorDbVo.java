package com.skcto.skknowledge.vo;

import lombok.Data;

@Data
public class VectorDbVo {

    private Integer id;

    /**
     * 向量库类型：MILVUS/weaviate
     */
    private String vectorName;

    /**
     * 服务地址
     */
    private String host;

    /**
     * 端口
     */
    private Integer port;


}