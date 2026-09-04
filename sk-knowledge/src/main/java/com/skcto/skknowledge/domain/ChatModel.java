package com.skcto.skknowledge.domain;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

/**
 * 聊天模型
 * @TableName chat_model
 */
@TableName(value ="chat_model")
@Data
public class ChatModel extends BaseEntity {

    /**
     * 模型分类
     */
    private String category;

    /**
     * 模型名称
     */
    @TableField(value = "`name`")
    private String name;

    /**
     * 模型描述
     */
    @TableField(value = "`describe`")
    private String describe;

    /**
     * 模型价格
     */
    private Double price;

    /**
     * 模型类型
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