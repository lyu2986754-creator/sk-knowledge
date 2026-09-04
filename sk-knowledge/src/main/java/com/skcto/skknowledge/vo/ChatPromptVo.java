package com.skcto.skknowledge.vo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

import java.util.Date;

/**
 * 提示词模板表
 * @TableName chat_prompt
 */
@Data
public class ChatPromptVo {
    /**
     * 主键
     */
    private Integer id;

    /**
     * 提示词模板名称
     */
    private String chatName;

    /**
     * 提示词模板内容
     */
    private String chatContent;

    /**
     * 提示词分类，knowledge 知识库类型，chat 对话类型，draw绘画类型 ...
     */
    private String category;


    /**
     * 备注
     */
    private String remark;

}