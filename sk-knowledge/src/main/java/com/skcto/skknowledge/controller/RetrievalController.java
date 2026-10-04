package com.skcto.skknowledge.controller;

import com.skcto.skknowledge.result.Result;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 只读检索接口。
 *
 * 为什么需要它：
 *   本项目的问答链路是"检索一次 + 生成一次"，检索策略固定。
 *   而 Agentic 编排需要自己决定「检索什么、检索几次」——
 *   这就要求把"检索"这一步单独暴露出来，让外部编排层能反复调用。
 *
 * 边界（重要）：
 *   1. 只读。不写入、不修改任何数据。
 *   2. 项目自身的问答链路**不经过**这里，走的是 QuestionAnswerAdvisor。
 *      删掉本接口，问答功能不受任何影响（符合 project.md R2：采集/编排必须旁路）。
 *   3. 它不理解"Agent"这个概念，只是一个纯粹的检索能力出口。
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/")
public class RetrievalController {

    private final VectorStore vectorStore;

    /**
     * 按查询语句检索知识库。
     *
     * @param query       查询语句（由调用方决定，可以是用户原话，也可以是改写后的）
     * @param knowledgeId 知识库 id
     * @param topK        返回条数，默认 10
     */
    @GetMapping("/retrieve")
    public Result retrieve(@RequestParam String query,
                           @RequestParam Integer knowledgeId,
                           @RequestParam(defaultValue = "10") Integer topK) {

        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        Filter.Expression expression =
                filterExpressionBuilder.eq("knowledgeId", knowledgeId.toString()).build();

        SearchRequest searchRequest = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .filterExpression(expression)
                .build();

        List<Map<String, Object>> items = new ArrayList<>();
        int rank = 1;
        for (Document document : vectorStore.similaritySearch(searchRequest)) {
            Map<String, Object> one = new HashMap<>();
            one.put("rank", rank++);
            one.put("content", document.getText());
            Object source = document.getMetadata() == null
                    ? null
                    : document.getMetadata().get("source");
            one.put("source", source == null ? "" : source.toString());
            items.add(one);
        }

        return Result.OK(items);
    }
}

