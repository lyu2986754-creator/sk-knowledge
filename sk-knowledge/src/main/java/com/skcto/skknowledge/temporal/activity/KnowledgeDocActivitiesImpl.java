package com.skcto.skknowledge.temporal.activity;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.skcto.skknowledge.domain.KnowledgeDoc;
import com.skcto.skknowledge.mapper.KnowledgeDocMapper;
import com.skcto.skknowledge.service.MinioService;
import com.skcto.skknowledge.service.VectorService;
import io.temporal.spring.boot.ActivityImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.io.InputStream;


/***
 将文本块存储到weaviate中
 *  将数据放入mysql中
 *
 *  activity中的方法需要保证幂等性，不要使用随机数之类的
 *  gRPC 传参大小默认时4MB，activity中的方法的参数大小，单个参数不能超过2MB，总共不要超过4MB
 *  方法的参数可以序列化
 *  activity会在temporal中的worker线程中执行   controller  -》 service -》 activity
 */
@Component
@ActivityImpl
@RequiredArgsConstructor
public class KnowledgeDocActivitiesImpl implements KnowledgeDocActivities {

    private final MinioService minioService;

    private final VectorService vectorService;

    private final KnowledgeDocMapper knowledgeDocMapper;


    /**
     * 将pdf存入minio 补偿
     */
    @Override
    public void uploadFileCompensate(String url) {

        try {
            int index = url.indexOf("/");
            String bucketName = url.substring(0, index);//桶的名字
            String objectName = url.substring(index + 1);//文件对象名
            minioService.deleteFile(bucketName, objectName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    /*
        将文本块存储到weaviate中
     */
    @Override
    public void storeTextToDB(KnowledgeDoc knowledgeDoc) {
        //获取minio中文件的地址url  knowledge/123.pdf
        String url = knowledgeDoc.getUrl();
        int index = url.indexOf("/");
        String bucketName = url.substring(0, index);//桶的名字
        String objectName = url.substring(index + 1);//文件对象名

        //从minio中读取文件流
        InputStream inputStream = minioService.downloadFile(bucketName, objectName);

        //将文本块存储到weaviate中
        vectorService.storeText(knowledgeDoc.getKnowledgeId(), inputStream, knowledgeDoc.getUrl());
    }

    /**
     * 将文本块存储到weaviate中 补偿
     */
    @Override
    public void storeTextToDBCompensation(KnowledgeDoc knowledgeDoc) {
        vectorService.deleteBySourceUrl(knowledgeDoc.getUrl());
    }

    /**
     * 将数据放入mysql中
     */
    @Override
    public void saveToDB(KnowledgeDoc knowledgeDoc) {
        knowledgeDocMapper.insert(knowledgeDoc);
    }

    /**
     * 将数据放入mysql中 补偿
     */
    @Override
    public void saveToDBCompensate(KnowledgeDoc knowledgeDoc) {
        //根据url删除
        QueryWrapper<KnowledgeDoc> objectQueryWrapper = new QueryWrapper<>();
        objectQueryWrapper.eq("url", knowledgeDoc.getUrl());
        knowledgeDocMapper.delete(objectQueryWrapper);
    }
}