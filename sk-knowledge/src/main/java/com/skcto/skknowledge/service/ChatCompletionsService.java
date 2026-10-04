package com.skcto.skknowledge.service;

import com.skcto.skknowledge.dto.ChatCompletionsDTO;
import com.skcto.skknowledge.dto.ChatRoundDTO;
import com.skcto.skknowledge.vo.ChatCompletionsVo;
import com.skcto.skknowledge.vo.ChatContextIdVo;
import com.skcto.skknowledge.vo.ChatWindowVo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ChatCompletionsService {

    List<ChatWindowVo> selectChatWindows();

    List<ChatCompletionsVo> selectChatCompletions(String windowId);

    String handleSessionId(ChatCompletionsDTO chatCompletionsDTO);

    void streamResponse(String sessionId, Integer knowledgeId, SseEmitter sseEmitter);

    /**
     * 保存一轮由外部编排生成完成的问答。
     *
     * 只做落库，不做检索与生成——那些由调用方完成。
     */
    ChatContextIdVo saveCompletedRound(ChatRoundDTO round);
}
