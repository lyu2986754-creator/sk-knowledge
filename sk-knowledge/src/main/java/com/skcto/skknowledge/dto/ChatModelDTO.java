package com.skcto.skknowledge.dto;

import lombok.Data;

@Data
public class ChatModelDTO {

    private Integer id;

    /**
     * 模型分类
     */
    private String category;

    /**
     * 模型名称
     */
    private String name;

    /**
     * 模型描述
     */
    private String describe;

    /**
     * 模型价格
     */
    private Double price;

    /**
     * 计费类型
     */
    private String type;

    /**
     * 请求地址
     */
    private String apiHost;

    /**
     * 密钥
     */
    private String apiKey;



    /**
     * 备注
     */
    private String remark;
}
