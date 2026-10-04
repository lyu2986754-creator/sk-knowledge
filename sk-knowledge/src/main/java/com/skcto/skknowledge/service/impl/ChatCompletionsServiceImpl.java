package com.skcto.skknowledge.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.skcto.skknowledge.constant.Constant;
import com.skcto.skknowledge.domain.ChatCompletions;
import com.skcto.skknowledge.domain.ChatModel;
import com.skcto.skknowledge.domain.ChatWindow;
import com.skcto.skknowledge.domain.User;
import com.skcto.skknowledge.dto.ChatCompletionsDTO;
import com.skcto.skknowledge.dto.ChatRoundDTO;
import com.skcto.skknowledge.factory.ChatClientFactory;
import com.skcto.skknowledge.mapstuct.ChatCompletionsMapstruct;
import com.skcto.skknowledge.mapstuct.ChatWindowMapstruct;
import com.skcto.skknowledge.service.ChatCompletionsService;
import com.skcto.skknowledge.service.ChatModelService;
import com.skcto.skknowledge.util.LoginInfoUtil;
import com.skcto.skknowledge.vo.ChatCompletionsVo;
import com.skcto.skknowledge.vo.ChatContextIdVo;
import com.skcto.skknowledge.vo.ChatWindowVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.ChatClientResponse;
import org.springframework.ai.chat.client.advisor.vectorstore.QuestionAnswerAdvisor;
import org.springframework.ai.chat.metadata.Usage;
import org.springframework.ai.chat.messages.*;
import org.springframework.ai.chat.model.ChatResponse;
import org.springframework.ai.chat.prompt.PromptTemplate;
import org.springframework.ai.template.st.StTemplateRenderer;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatCompletionsServiceImpl implements ChatCompletionsService {

    private final MongoTemplate mongoTemplate;

    private final ChatModelService chatModelService;

    private final ChatCompletionsMapstruct chatCompletionsMapstruct;

    private final ChatWindowMapstruct chatWindowMapstruct;

    private final VectorStore vectorStore;

    private final RedisTemplate redisTemplate;

    private final ObjectMapper objectMapper;

    /**
     * 从mongodb中国查询数据
     * @return
     */
    @Override
    public List<ChatWindowVo> selectChatWindows() {
        List<ChatWindow> chatWindows = mongoTemplate.find(new Query().with(Sort.by(Sort.Direction.DESC, "updateTime")), ChatWindow.class);

        return chatWindowMapstruct.entityToVo(chatWindows);
    }

    /**
     * 从mongodb中国查询聊天数据
     */
    @Override
    public List<ChatCompletionsVo> selectChatCompletions(String windowId) {
        List<ChatCompletions> chatCompletions = mongoTemplate.find(new Query().addCriteria(Criteria.where("windowId").is(windowId)), ChatCompletions.class);

        return chatCompletionsMapstruct.entityToVo(chatCompletions);
    }

    @Override
    public String handleSessionId(ChatCompletionsDTO chatCompletionsDTO) {
        String sessionId = UUID.randomUUID().toString();

        User user = LoginInfoUtil.getCurrentLoginUser();

        chatCompletionsDTO.setLoginUserId(user.getId());

        //放入redis
        redisTemplate.opsForValue().set(Constant.SSE_SESSION + sessionId, chatCompletionsDTO, 5, TimeUnit.MINUTES);

        return sessionId;
    }

    /**
     * 处理提问
     *
     * @param sessionId
     * @param knowledgeId
     * @param sseEmitter
     */
    @Async("chatTaskExecutor")//spring异步处理
    @Override
    public void streamResponse(String sessionId, Integer knowledgeId, SseEmitter sseEmitter) {

        try {
            //从redis中根据sessionId获取数据
            ChatCompletionsDTO chatCompletionsDTO = (ChatCompletionsDTO) redisTemplate.opsForValue().get(Constant.SSE_SESSION + sessionId);
            if (chatCompletionsDTO == null) {
                throw new RuntimeException("sessionId不存在");
            }

            //查询聊天模型
            ChatModel chatModel = chatModelService.getById(chatCompletionsDTO.getModelId());

            //构建聊天模型对象，生成标题
            ChatClient chatClientTittle = ChatClientFactory.createOpenAiCompatibleClient(chatModel.getApiHost(), chatModel.getApiKey(), chatModel.getName());

            //判断窗口是否第一次聊天
            ChatWindow chatWindow = null;
            if (!StringUtils.hasText(chatCompletionsDTO.getWindowId())){
                //第一次
                chatWindow = new ChatWindow();
                chatWindow.setCreateTime(new Date());
                chatWindow.setUpdateTime(new Date());
                chatWindow.setUserId(chatCompletionsDTO.getLoginUserId());

                //利用大模型来生成标题
                ChatResponse chatResponse = chatClientTittle.prompt()
                        .system("你是AI助手，用简短的语句作答")
                        .user("为该内容生成一个简短的标题：" + chatCompletionsDTO.getContent())
                        .call()
                        .chatResponse();
                //获取大模型返回的标题
                chatWindow.setTittle(chatResponse.getResult().getOutput().getText());

                //存入mongodb中
                mongoTemplate.insert(chatWindow);
                chatCompletionsDTO.setWindowId(chatWindow.getId());

            }else {
                //从mongodb中查询窗口数据
                chatWindow = mongoTemplate.findById(chatCompletionsDTO.getWindowId(), ChatWindow.class);
            }

            //处理标题数据给前端返回
            String userQuestionId = ObjectId.get().toString();
            String assistantId = ObjectId.get().toString();
            String chatWindowId = chatWindow.getId();

            ChatContextIdVo chatContextIdVo = new ChatContextIdVo();
            chatContextIdVo.setUserQuestionId(userQuestionId);
            chatContextIdVo.setAssistantId(assistantId);
            chatContextIdVo.setChatWindowId(chatWindowId);
            chatContextIdVo.setChatWindowTittle(chatWindow.getTittle());

            String jsonStr = objectMapper.writeValueAsString(chatContextIdVo);

            //利用sse将json标题发给前端
            sseEmitter.send(SseEmitter.event().name("chatContextIdVo").data(jsonStr));


            //构建提示词模板
            PromptTemplate customPromptTemplate = PromptTemplate.builder()
                    .renderer(StTemplateRenderer.builder().startDelimiterToken('<').endDelimiterToken('>').build())
                    .template("""
                            问题:<query>
                            基于下面上下文回答问题:
                            <question_answer_context>
                            """)
                    .build();

            //召回 根据知识库进行召回
            FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
            Filter.Expression expression = filterExpressionBuilder.eq("knowledgeId", knowledgeId.toString()).build();

            //构建请求
            SearchRequest searchRequest = SearchRequest.builder()
                    .query("dummy query to get all documents") //占位符 匹配查询
                    .topK(10)
                    .filterExpression(expression)
                    .build();

            //构建advisor
            QuestionAnswerAdvisor qaAdvisor = QuestionAnswerAdvisor.builder(vectorStore) //传入vectorStore 操作我们设置好的向量数据库
                    .promptTemplate(customPromptTemplate) //提示词模板
                    .searchRequest(searchRequest)//向量数据库的条件检索
                    .build();

            //从mongodb中查询聊天上下文
            Query query = new Query().addCriteria(Criteria.where("windowId").is(chatWindow.getId()));
            //查询本次聊天窗口的上下文
            List<ChatCompletions> chatCompletionsList = mongoTemplate.find(query, ChatCompletions.class);

            //处理上面集合List<ChatCompletions>转成List<Message>
            //spring ai 可以看懂的历史记录信息
            List<Message> historyMessages = chatCompletionsList.stream()
                    .map(map -> {
                        //判断角色
                        String role = map.getRole();
                        String content = map.getContent();
                        switch (role) {
                            case "user":
                                return new UserMessage(content);
                            case "assistant":
                                return new AssistantMessage(content);
                            case "system":
                                return new SystemMessage(content);
                            default:
                                throw new RuntimeException("未知角色");
                        }
                    }).collect(Collectors.toList());

            //加入本次提问数据
            historyMessages.add(new UserMessage(chatCompletionsDTO.getContent()));

            //获取大模型 回答用户的提问
            ChatClient chatClientAnswer = ChatClientFactory.createOpenAiCompatibleClient(chatModel.getApiHost(), chatModel.getApiKey(), chatModel.getName());

            //回答结果
            StringBuffer content = new StringBuffer();

            // ===== 诊断采集（只读，不介入问答链路）=====
            // 采集真实的召回片段与 token 用量，通过独立的 SSE 事件推给评测平台。
            // 为什么必须采集：仅凭最终答案无法区分「模型不懂法」和「检索漏了那一条」，
            // 而这个区别决定了该改 prompt 还是该改检索策略。
            List<Map<String, Object>> retrievedChunks = new ArrayList<>();
            final Integer[] promptTokens = {null};
            final Integer[] completionTokens = {null};
            final long generateStartMs = System.currentTimeMillis();
            final long[] firstTokenMs = {0L};

            //使用大模型进行回答
            chatClientAnswer
                    .prompt(chatCompletionsDTO.getContent())
                    .system("你是一个智能助手，可以基于历史对话回答问题")
                    .messages(historyMessages)//历史上下文聊天记录
                    .advisors(qaAdvisor)//向量数据库召回
                    .stream()
                    // 必须用 chatClientResponse()：只有它带响应上下文（context()），
                    // chatResponse() 返回的 ChatResponse 里没有上下文，拿不到召回片段。
                    .chatClientResponse()
                    .subscribe(
                            chatClientResponse -> {
                                // 召回片段：QuestionAnswerAdvisor 会放进响应上下文，取首次即可
                                if (retrievedChunks.isEmpty()) {
                                    collectRetrievedChunks(chatClientResponse, retrievedChunks);
                                }

                                ChatResponse chatResponse = chatClientResponse.chatResponse();
                                if (chatResponse != null && chatResponse.getMetadata() != null) {
                                    Usage usage = chatResponse.getMetadata().getUsage();
                                    if (usage != null && usage.getTotalTokens() != null
                                            && usage.getTotalTokens() > 0) {
                                        promptTokens[0] = usage.getPromptTokens();
                                        completionTokens[0] = usage.getCompletionTokens();
                                    }
                                }

                                String chunk = chatResponse == null || chatResponse.getResult() == null
                                        ? null
                                        : chatResponse.getResult().getOutput().getText();
                                if (chunk == null || chunk.isEmpty()) {
                                    return;
                                }
                                if (firstTokenMs[0] == 0L) {
                                    firstTokenMs[0] = System.currentTimeMillis();
                                }
                                content.append(chunk);
                                //发送sse消息给前端
                                try {
                                    sseEmitter.send(SseEmitter.event()
                                            .name("content")
                                            .data(chunk));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                            },
                            error -> {
                                log.error(error.toString());
                                sseEmitter.completeWithError(error);
                            },
                            ()->{

                                try {
                                    //sse结束
                                    redisTemplate.delete(Constant.SSE_SESSION + sessionId);

                                    // 先推诊断再推 done：评测平台据此拿到真实召回与用量
                                    sendDiagnostics(sseEmitter, retrievedChunks, promptTokens[0],
                                            completionTokens[0], generateStartMs, firstTokenMs[0]);

                                    //存储相关聊天上下文数据到mongodb中
                                    ChatCompletionsVo chatCompletionsVo = saveChatCompletions(chatCompletionsDTO,content.toString(),userQuestionId,assistantId);

                                    sseEmitter.send(SseEmitter.event().name("done")
                                            .data(chatCompletionsVo));
                                } catch (IOException e) {
                                    throw new RuntimeException(e);
                                }
                                sseEmitter.complete();//sse结束

                            }
                    );

        } catch (Exception e) {
        throw new RuntimeException(e);
        }
    }

    /**
     * 保存一轮由外部编排（如 Agentic 服务）生成完成的问答。
     *
     * 只负责落库：窗口的创建与标题、消息的结构、窗口时间的更新都在这里统一处理。
     * 调用方不需要知道 MongoDB 的集合与字段形状。
     */
    @Override
    public ChatContextIdVo saveCompletedRound(ChatRoundDTO round) {
        User user = LoginInfoUtil.getCurrentLoginUser();

        // 创建或复用会话窗口
        ChatWindow chatWindow = null;
        if (StringUtils.hasText(round.getWindowId())) {
            chatWindow = mongoTemplate.findById(round.getWindowId(), ChatWindow.class);
        }
        if (chatWindow == null) {
            // 没给 windowId，或给的 id 在库里不存在（已删除 / 调用方给了临时 id）：
            // 一律新建窗口，而不是抛异常。
            // 取舍：宁可多出一个会话，也不要让整轮对话因为 id 对不上而丢失。
            chatWindow = new ChatWindow();
            chatWindow.setUserId(user.getId());
            chatWindow.setCreateTime(new Date());
            chatWindow.setUpdateTime(new Date());
            // 外部编排不做标题生成，直接用问题截断，省一次模型调用
            String question = round.getQuestion() == null ? "" : round.getQuestion();
            chatWindow.setTittle(question.length() > 18 ? question.substring(0, 18) : question);
            mongoTemplate.insert(chatWindow);
        }

        String userQuestionId = ObjectId.get().toString();
        String assistantId = ObjectId.get().toString();
        Date now = new Date();

        ChatCompletions userMessage = new ChatCompletions();
        userMessage.setId(userQuestionId);
        userMessage.setWindowId(chatWindow.getId());
        userMessage.setKnowledgeId(round.getKnowledgeId());
        userMessage.setModelId(round.getModelId());
        userMessage.setContent(round.getQuestion());
        userMessage.setRole("user");
        userMessage.setCreateTime(now);

        ChatCompletions assistantMessage = new ChatCompletions();
        assistantMessage.setId(assistantId);
        assistantMessage.setWindowId(chatWindow.getId());
        assistantMessage.setKnowledgeId(round.getKnowledgeId());
        assistantMessage.setModelId(round.getModelId());
        assistantMessage.setContent(round.getAnswer());
        assistantMessage.setRole("assistant");
        assistantMessage.setCreateTime(now);

        mongoTemplate.insertAll(List.of(userMessage, assistantMessage));

        // 更新窗口的最后活动时间，让会话列表按最近使用排序
        Query query = new Query(Criteria.where("id").is(chatWindow.getId()));
        Update update = new Update();
        update.set("updateTime", now);
        mongoTemplate.updateFirst(query, update, ChatWindow.class);

        ChatContextIdVo vo = new ChatContextIdVo();
        vo.setUserQuestionId(userQuestionId);
        vo.setAssistantId(assistantId);
        vo.setChatWindowId(chatWindow.getId());
        vo.setChatWindowTittle(chatWindow.getTittle());
        return vo;
    }

    /**
     * 从响应上下文里取出 QuestionAnswerAdvisor 放入的召回文档。
     *
     * 只读：不改动检索结果，只是把它复制出来给评测平台看。
     */
    private void collectRetrievedChunks(ChatClientResponse chatClientResponse,
                                        List<Map<String, Object>> sink) {
        Map<String, Object> context = chatClientResponse.context();
        if (context == null) {
            return;
        }
        Object docs = context.get(QuestionAnswerAdvisor.RETRIEVED_DOCUMENTS);
        if (!(docs instanceof List<?> list)) {
            return;
        }
        int rank = 1;
        for (Object item : list) {
            if (!(item instanceof Document document)) {
                continue;
            }
            Map<String, Object> one = new HashMap<>();
            one.put("rank", rank++);
            one.put("content", truncate(document.getText(), 2000));
            Object source = document.getMetadata() == null
                    ? null
                    : document.getMetadata().get("source");
            one.put("source", source == null ? "" : source.toString());
            sink.add(one);
        }
    }

    /**
     * 把诊断信息作为独立的 SSE 事件推出。
     *
     * 刻意与主链路隔离：推送失败只记警告，绝不影响问答本身。
     */
    private void sendDiagnostics(SseEmitter sseEmitter, List<Map<String, Object>> chunks,
                                 Integer promptTokens, Integer completionTokens,
                                 long generateStartMs, long firstTokenMs) {
        Map<String, Object> diagnostics = new HashMap<>();
        diagnostics.put("retrievedChunks", chunks);
        diagnostics.put("promptTokens", promptTokens);
        diagnostics.put("completionTokens", completionTokens);
        diagnostics.put("generateMs", System.currentTimeMillis() - generateStartMs);
        diagnostics.put("firstTokenMs",
                firstTokenMs == 0L ? null : firstTokenMs - generateStartMs);
        try {
            sseEmitter.send(SseEmitter.event().name("diagnostics")
                    .data(objectMapper.writeValueAsString(diagnostics)));
        } catch (Exception e) {
            log.warn("推送诊断信息失败：{}", e.toString());
        }
    }

    private String truncate(String text, int max) {
        if (text == null) {
            return "";
        }
        return text.length() <= max ? text : text.substring(0, max);
    }

    /**
     * 保存上下文聊天记录
     * @param chatCompletionsDTO
     * @param content
     * @param userQuestionId
     * @param assistantId
     * @return
     */
    private ChatCompletionsVo saveChatCompletions(ChatCompletionsDTO chatCompletionsDTO, String content, String userQuestionId, String assistantId) {

        //一轮对话的集合 包含本次提问和回答
        List<ChatCompletionsDTO> oneRoundChatCompletions = new ArrayList<>();

        //提问对象
        ChatCompletionsDTO chatCompletionsUser = chatCompletionsDTO;
        chatCompletionsUser.setId(userQuestionId);
        chatCompletionsUser.setRole(MessageType.USER.getValue());
        chatCompletionsUser.setCreateTime(new Date());
        chatCompletionsUser.setWindowId(chatCompletionsDTO.getWindowId());
        oneRoundChatCompletions.add(chatCompletionsUser);

        //回答对象
        ChatCompletionsDTO chatCompletionsAssistant = new ChatCompletionsDTO();
        chatCompletionsAssistant.setId(assistantId);
        chatCompletionsAssistant.setModelId(chatCompletionsDTO.getModelId());
        chatCompletionsAssistant.setKnowledgeId(chatCompletionsDTO.getKnowledgeId());
        chatCompletionsAssistant.setContent(content);//回答的内容
        chatCompletionsAssistant.setCreateTime(new Date());
        chatCompletionsAssistant.setRole(MessageType.ASSISTANT.getValue());
        chatCompletionsAssistant.setWindowId(chatCompletionsDTO.getWindowId());
        oneRoundChatCompletions.add(chatCompletionsAssistant);

        //转换集合
        List<ChatCompletions> chatCompletionsList = chatCompletionsMapstruct.dtoToEntity(oneRoundChatCompletions);
        //将数据放入mongodb中
        mongoTemplate.insertAll(chatCompletionsList);

        //更新当前窗口的时间
        Query query = new Query(Criteria.where("id").is(chatCompletionsDTO.getWindowId()));
        Update update = new Update();
        update.set("updateTime", new Date());
        mongoTemplate.updateFirst(query, update, ChatWindow.class);

        ChatCompletionsVo chatCompletionsVo = chatCompletionsMapstruct.dtoToVo(chatCompletionsDTO);

        chatCompletionsVo.setRole(MessageType.ASSISTANT.getValue());
        chatCompletionsVo.setContent(content);
        chatCompletionsVo.setCreateTime(new Date());
        chatCompletionsVo.setId(chatCompletionsAssistant.getId());
        chatCompletionsVo.setWindowId(chatCompletionsDTO.getWindowId());

        return chatCompletionsVo;
        }
    }
