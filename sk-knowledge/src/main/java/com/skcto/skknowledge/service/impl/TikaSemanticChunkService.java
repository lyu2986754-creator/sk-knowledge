package com.skcto.skknowledge.service.impl;


import jakarta.annotation.Resource;
import lombok.RequiredArgsConstructor;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.AutoDetectParser;
import org.apache.tika.sax.ToXMLContentHandler;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.ai.embedding.EmbeddingModel;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class TikaSemanticChunkService {

    @Resource
    private EmbeddingModel embeddingModel;

    @Value("${chunk.threshold:0.8}") // 设置默认值，避免配置缺失报错
    private double similarityThreshold;

    @Value("${chunk.token:500}") // 设置默认值
    private int maxChunkTokens;

    // 1. 匹配句子结束标点的正则（捕获标点，保留完整句子）
    // 匹配规则：任意字符 + 句子结束标点（。！？；.!?;）
    private static final Pattern SENTENCE_PATTERN = Pattern.compile(".*?[。！？；.!?;]");
    // 2. 匹配所有空白符（换行、制表符、多个空格）的正则
    private static final Pattern WHITESPACE_PATTERN = Pattern.compile("\\s+");

    /**
     * 提取pdf到chunk
     */

    public List<String> pdfToChunk(InputStream inputStream){

        //提取pdf中的文本数据
        String pdfText = extractPdfText(inputStream);

        //拆分句子
        List<String> sentences = splitTextIntoSentences(pdfText);

        //获取chunk
        List<String> chunks = textToChunks(sentences);

        return chunks;
    }




    /**
     * 提取pdf中的文本数据
     */
    private String extractPdfText(InputStream inputStream) {
        try {
            Tika tika = new Tika();
            return tika.parseToString(inputStream);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TikaException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 拆分文本数据按照标点符号拆分为句子
     */
    public List<String> splitTextIntoSentences(String pdfText) {
        //存储句子的集合
        List<String> sentences = new ArrayList<>();

        //去除空白（换行 制表符 空格字符）
        String processedText = WHITESPACE_PATTERN.matcher(pdfText).replaceAll("").trim();
        if(processedText.isEmpty()){
            return sentences;
        }

        //根据标点符号匹配句子
        Matcher matcher = SENTENCE_PATTERN.matcher(processedText);
        while(matcher.find()){
            String sentence = matcher.group().trim();
            if(!sentence.isEmpty()){
                //将句子放入集合
                sentences.add(sentence);
            }
        }

        //处理最后一段没有结束标点的句子
        int lastEnd = 0;
        for(int i = 0 ; i < sentences.size() ; i++){
            lastEnd +=  sentences.get(i).length();
        }

        //整个文档字符串数据根据长度截取，最后一句话
        String remainingText = processedText.substring(lastEnd).trim();
        if(!remainingText.isEmpty()){
            sentences.add(remainingText);
        }

        return sentences;
    }

    /**
     * 获取句子向量
     * 按照相似度合并块chunk
     */
    private List<String> textToChunks(List<String> sentences) {

        //获取句子向量 使用向量模型
        List<float[]> embed = embeddingModel.embed(sentences);

        //根据向量相似度合并块
        List<String> chunks = mergeBySimilarity(sentences, embed);

        return  chunks;
    }


    /**
     *按照相似度合并块chunk
     * sentences 句子集合
     * embed 向量集合
     */
    public List<String> mergeBySimilarity(List<String> sentences, List<float[]> embed) {
        //存储块数据
        List<String> chunkList = new ArrayList<>();

        //拼接相似度符合阈值的句子
        StringBuilder currentChunk = new StringBuilder();

        for(int i = 0 ; i < sentences.size() ; i++){
            //获取句子
            String sentence = sentences.get(i);

            if(currentChunk.length() > 0){
                //上个句子和当前句子做相似度比对
                float[] prevVec = embed.get(i - 1);
                float[] currVec = embed.get(i);

                //计算相似度 越接近1，越相似
                float similarity = cosineSimilarity(prevVec, currVec);

                //如果相似度小于阈值，则将句子添加到块中
                if(similarity < similarityThreshold || (currentChunk.length() + sentence.length()) > maxChunkTokens ){
                    String chunk = currentChunk.toString().trim();
                    if(!chunk.isEmpty()){
                        //将这个块数据放入集合中
                        chunkList.add(chunk);
                    }
                    currentChunk.setLength(0);
                }
            }

            //在同一个chunk中的句子中，使用空格隔开
            if (currentChunk.length() > 0){
                currentChunk.append(" ");
            }

            //当前句子添加到块中 当前句子和前面句子相似度较高
            currentChunk.append(sentence);

        }
        //处理最后的块
        String lastChunk = currentChunk.toString().trim();
        if(!lastChunk.isEmpty()){
            chunkList.add(lastChunk);
        }

        return  chunkList;

    }

    private float cosineSimilarity(float[] a, float[] b) {
        if (a == null || b == null || a.length != b.length) {
            return 0.0F;
        }

        float dotProduct = 0.0F;//点积
        float normA = 0.0F;//范数
        float normB = 0.0F;

        //计算点积和范数
        for (int i = 0; i < a.length; i++) {
            dotProduct += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        //避免除以0
        if(normA ==0.0F || normB ==0.0F){
            return 0.0F;
        }

        //计算相似度
        return dotProduct / (float) (Math.sqrt(normA) * Math.sqrt(normB));
    }

}
