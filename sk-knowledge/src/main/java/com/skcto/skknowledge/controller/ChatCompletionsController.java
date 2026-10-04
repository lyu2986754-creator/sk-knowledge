package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.dto.ChatCompletionsDTO;
import com.skcto.skknowledge.dto.ChatRoundDTO;
import com.skcto.skknowledge.result.Result;
import com.skcto.skknowledge.vo.ChatContextIdVo;
import com.skcto.skknowledge.service.ChatCompletionsService;
import com.skcto.skknowledge.vo.ChatCompletionsVo;
import com.skcto.skknowledge.vo.ChatWindowVo;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/chat")
public class ChatCompletionsController {

    private final ChatCompletionsService chatCompletionsService;


    /**
     * 查询聊天窗口
     */
    @GetMapping("/window")
    @ResponseBody
    public Result queryChatWindows(){
        List<ChatWindowVo> chatWindowVoList = chatCompletionsService.selectChatWindows();

        return Result.OK(chatWindowVoList);
    }

    /**
     * 查询聊天记录
     */
    @GetMapping("/completions")
    @ResponseBody
    public Result queryChatCompletions(@RequestParam String windowId) {
        List<ChatCompletionsVo> chatCompletionsVoList = chatCompletionsService.selectChatCompletions(windowId);

        return Result.OK(chatCompletionsVoList);
    }

    /**
     * 创建会话  接收前端发送的问题，生成sessionId
     */
    @PostMapping("/completions")
    @ResponseBody
    public Result chatCompletions(@RequestBody ChatCompletionsDTO chatCompletionsDTO) {
        String sessionId = chatCompletionsService.handleSessionId(chatCompletionsDTO);

        return Result.OK(sessionId);
    }

    /**
     * 保存一轮由外部编排（Agentic 服务）生成完成的问答。
     *
     * 存在的意义：让编排层自己完成检索与生成后，把结果交回本服务落库，
     * 从而复用同一套会话数据结构——前端的历史列表不需要区分是谁生成的。
     */
    @PostMapping("/round")
    @ResponseBody
    public Result saveRound(@RequestBody ChatRoundDTO round) {
        ChatContextIdVo vo = chatCompletionsService.saveCompletedRound(round);
        return Result.OK(vo);
    }

    /**
     * 创建sse连接
     */
    @GetMapping("/stream/{sessionId}/{knowledgeId}")
    public SseEmitter streamResponse(@PathVariable String sessionId, @PathVariable Integer knowledgeId, HttpServletResponse response) {
        //设置响应头
        response.setContentType(MediaType.TEXT_EVENT_STREAM_VALUE);
        response.setCharacterEncoding("UTF-8");
        SseEmitter sseEmitter = new SseEmitter(5 * 60 * 1000L);//超时时间5分钟

        //异步调用
        chatCompletionsService.streamResponse(sessionId, knowledgeId, sseEmitter);

        return sseEmitter;
    }


}
