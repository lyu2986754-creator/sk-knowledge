package com.skcto.skknowledge.service.impl;


import com.skcto.skknowledge.chunk.Chunking;
import com.skcto.skknowledge.constant.Constant;
import com.skcto.skknowledge.service.VectorService;
import io.weaviate.client.WeaviateClient;
import io.weaviate.client.base.Result;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.ai.vectorstore.filter.Filter;
import org.springframework.ai.vectorstore.filter.FilterExpressionBuilder;
import org.springframework.stereotype.Service;

import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class VectorServiceImpl implements VectorService {

    private final Chunking chunking;

    private final VectorStore vectorStore;

    private final WeaviateClient weaviateClient;

    private final TikaSemanticChunkService tikaSemanticChunkService;


    /**
     * 将pdf中提取出的文本块chunk转成向量数据之后存入weaviate中
     */
    @Override
    public void storeText(Integer knowledgeId, InputStream inputStream, String sourceUrl){
        //获取chunk
        List<String> chunks = tikaSemanticChunkService.pdfToChunk(inputStream);

        //存储document列表
        List<Document> documents = new ArrayList<>();

        for(String chunk : chunks){
            //查看该chunk在weaviate中是否存在相似度较高的数据
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(chunk)
                    .topK(1)
                    .similarityThreshold(Constant.SIMILARITY_THRESHOLD)
                    .build();

            List<Document> docList = vectorStore.similaritySearch(searchRequest);

            if(!docList.isEmpty()){
                //存在相似度较高的数据，跳过
                continue;
            }

            //构建document
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", sourceUrl);//pdf文件在minio中的url
            metadata.put("knowledgeId", knowledgeId.toString());//知识库id

            Document document = new Document(
                    UUID.randomUUID().toString(),//主键
                    chunk,//文本块
                    metadata//元数据
            );

            //放入集合
            documents.add(document);
        }

        //将数据存入weaviate中
        vectorStore.add(documents);
    }

    @Override
    public void storeText(Integer knowledgeId, List<String> chunks, String sourceUrl){

        //存储document列表
        List<Document> documents = new ArrayList<>();

        for(String chunk : chunks){
            //查看该chunk在weaviate中是否存在相似度较高的数据
            SearchRequest searchRequest = SearchRequest.builder()
                    .query(chunk)
                    .topK(1)
                    .similarityThreshold(Constant.SIMILARITY_THRESHOLD)
                    .build();

            List<Document> docList = vectorStore.similaritySearch(searchRequest);

            if(!docList.isEmpty()){
                //存在相似度较高的数据，跳过
                continue;
            }

            //构建document
            Map<String, Object> metadata = new HashMap<>();
            metadata.put("source", sourceUrl);//pdf文件在minio中的url
            metadata.put("knowledgeId", knowledgeId.toString());//知识库id

            Document document = new Document(
                    UUID.randomUUID().toString(),//主键
                    chunk,//文本块
                    metadata//元数据
            );

            //放入集合
            documents.add(document);
        }

        //将数据存入weaviate中
        vectorStore.add(documents);
    }


    /**
     * 删除指定url的向量数据
     */
    @Override
    public void deleteBySourceUrl(String url) {
        FilterExpressionBuilder filterExpressionBuilder = new FilterExpressionBuilder();
        //条件
        Filter.Expression expression = filterExpressionBuilder.eq("source", url).build();

        //构建请求
        SearchRequest searchRequest = SearchRequest.builder()
                .query("dummy query to get all documents")//占位符  这里不是做向量相似度查询，而是匹配查询
                .topK(1000)
                .filterExpression(expression)
                .build();

        //查询向量主键
        List<String> docList = vectorStore.similaritySearch(searchRequest)
                .stream()
                .map(Document::getId)
                .collect(Collectors.toList());

        //删除向量数据
        if(!docList.isEmpty()){
            vectorStore.delete(docList);
        }
    }

    /**
     * 删除schma的方法
     */
    @Override
    public boolean deleteSchema(String className){
        Result<Boolean> deleteResult = weaviateClient.schema().classDeleter().withClassName(className).run();

        return deleteResult.hasErrors();
    }
}
