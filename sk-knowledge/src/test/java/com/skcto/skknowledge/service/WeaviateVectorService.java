package com.skcto.skknowledge.service;

import com.skcto.skknowledge.eneity.Book;
import org.springframework.ai.document.Document;

import java.util.List;


public interface WeaviateVectorService {
    void save(Book book);

    List<Document> similaritySearch(String query, int topK, double threshold);

    void update(Book book);

    void delete(String id);
}
