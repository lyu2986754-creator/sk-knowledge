package com.skcto.skknowledge.service;

import org.springframework.ai.document.Document;

import java.io.InputStream;
import java.util.List;
import java.util.Map;

public interface VectorService {

    void storeText(Integer knowledgeId, InputStream inputStream, String sourceUrl);

    void storeText(Integer knowledgeId, List<String> chunks, String sourceUrl);

    void deleteBySourceUrl(String url);

    boolean deleteSchema(String className);
}
