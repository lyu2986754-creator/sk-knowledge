package com.skcto.skknowledge.dto;

import lombok.Data;

import java.util.Date;

@Data
public class ChatWindowDTO {
    private String id;
    //用户id
    private int userId;
    //tittle 聊天窗口标题
    private String tittle;
    //创建时间
    private Date createTime;
    //最后更新时间
    private Date updateTime;

}
