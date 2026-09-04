package com.skcto.skknowledge.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Date;

@Document("chat_window")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatWindow {

    @Id
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
