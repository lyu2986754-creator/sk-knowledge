package com.skcto.skknowledge.domain;

import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 
 * @TableName vector_db
 */
@TableName(value ="vector_db")
@Data
public class VectorDb extends BaseEntity{

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

    /**
     * 认证密钥（如Qdrant的API Key）
     */
    private String apiKey;

    /**
     * 协议(http/https)
     */
    private String protocol;

    /**
     * 用户名
     */
    private String username;

    /**
     * 密码
     */
    private String password;

    /**
     * 状态：1-启用 0-禁用
     */
    private Integer status;

}