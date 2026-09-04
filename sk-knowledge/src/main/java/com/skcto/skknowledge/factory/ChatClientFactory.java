package com.skcto.skknowledge.factory;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.ai.openai.OpenAiChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;


public class ChatClientFactory {

    /**
     * 创建兼容OpenAi的聊天模型
     * @param apiHost
     * @param apiKey
     * @param name
     * @return
     */
    public static ChatClient createOpenAiCompatibleClient(String apiHost, String apiKey, String name) {

        OpenAiApi openAiApi = OpenAiApi.builder().baseUrl(apiHost).apiKey(apiKey).build();

        OpenAiChatOptions openAiChatOptions = OpenAiChatOptions.builder().model(name).temperature(0.3).frequencyPenalty(0.5).build();

        ChatModel chatModel = OpenAiChatModel.builder().openAiApi(openAiApi).defaultOptions(openAiChatOptions).build();

        return ChatClient.builder(chatModel).build();
    }
}