package com.skcto.skknowledge.service.impl;

import com.skcto.skknowledge.eneity.Book;
import com.skcto.skknowledge.service.WeaviateVectorService;
import jakarta.annotation.Resource;
import org.springframework.ai.document.Document;
import org.springframework.ai.vectorstore.SearchRequest;
import org.springframework.ai.vectorstore.VectorStore;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;

/**
 * 使用spring ai 操作向量数据库weaviate
 */
@Service
public class WeaviateVectorServiceImpl implements WeaviateVectorService {

    @Resource
    private VectorStore vectorStore;

    /**
     *添加向量
     *
     *
     */
    @Override
    public void save(Book book){
        vectorStore.add(List.of(toDocument(book)));
    }

    /**
     * 相似度查询
     * @param query  查询的数据
     * @param topK   查询的数据条数
     * @param threshold  阈值  越大越相似[0.0,1.0]
     * @return
     */
    public List<Document> similaritySearch(String query,int topK, double threshold){
        SearchRequest.Builder builder = SearchRequest.builder()
                .query(query)
                .topK(topK)
                .similarityThreshold(threshold);

        return  vectorStore.similaritySearch(builder.build());
    }

    /**
     * 先删除再添加
     */
    @Override
    public void update(Book book){
        vectorStore.delete(List.of(book.getId()));
        save(book);
    }

    /**
     * 删除向量
     */
    @Override
    public void delete(String id){
        vectorStore.delete(List.of(id));
    }

    /**
     * 转成document对象
     */
    private Document toDocument(Book book){
        HashMap<String, Object> metadata = new HashMap<>();
        metadata.put("author", book.getAuthor());
        metadata.put("price", book.getPrice());
        metadata.put("totalPages", book.getTotalPages());

        return new Document(book.getId(),book.getName(),metadata);
    }
}
