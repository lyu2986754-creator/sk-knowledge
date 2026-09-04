package com.skcto.skknowledge.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ChatContextIdVo {

    private String userQuestionId;
    private String assistantId;
    private String chatWindowId;
    private String chatWindowTittle;
}
