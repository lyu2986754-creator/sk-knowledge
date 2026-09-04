package com.skcto.skknowledge.service;

import com.skcto.skknowledge.dto.ChatCompletionsDTO;
import com.skcto.skknowledge.vo.ChatCompletionsVo;
import com.skcto.skknowledge.vo.ChatWindowVo;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.List;

public interface ChatCompletionsService {

    List<ChatWindowVo> selectChatWindows();

    List<ChatCompletionsVo> selectChatCompletions(String windowId);

    String handleSessionId(ChatCompletionsDTO chatCompletionsDTO);

    void streamResponse(String sessionId, Integer knowledgeId, SseEmitter sseEmitter);
}
