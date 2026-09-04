package com.skcto.skknowledge.service.impl;

import com.skcto.skknowledge.service.MinioService;
import io.minio.*;
import io.minio.errors.*;
import lombok.RequiredArgsConstructor;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Value("${minio.bucketName}")
    private String defaultBucketName;  //桶的名字

    /**
     * 判断桶是否存在
     */
    @Override
    public boolean bucketExists(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        return minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build());
    }

    /**
     * 创建桶
     */
    @Override
    public void createBucket(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException {
        if(!bucketExists(bucketName)){
            minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
        }

    }

    /**
     * 上传文件
     * 文件去重 相同的文件不上传
     * 获取上传文件的md5   123.pdf
     */
    @Override
    public String uploadFile(MultipartFile file, String bucketName) throws Exception{
        //若桶不存在则创建桶
        createBucket(bucketName);

        String fileMD5;

        //计算上传文件的md5 做去重
        try(InputStream inputStream = file.getInputStream();){
            fileMD5 = DigestUtils.md5Hex(inputStream);
        }

        //生成存储路径
        String originalFilename = file.getOriginalFilename();//获取原始文件名 简介.pdf
        String fileSuffix = originalFilename.substring(originalFilename.lastIndexOf("."));//文件后缀名
        String objectName = fileMD5 + fileSuffix;//拼接新的文件名

        //判断文件是否存在
        if(isFileExists(bucketName, objectName)){
            //存在
            return "exists";
        }

        //上传文件
        minioClient.putObject(PutObjectArgs
                .builder()
                .bucket(bucketName)
                .object(objectName)
                .stream(file.getInputStream(), file.getSize(), -1)
                .build());

        //相对路径
        return bucketName + "/" + objectName;



    }

    /**
     * 判断文件是否存在
     */
    private boolean isFileExists(String bucketName, String objectName){
        try {
            //获取文件信息
            minioClient.statObject(StatObjectArgs
                    .builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());

            return true;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return  false;
    }

    /**
     * 使用默认桶上传文件
     */
    @Override
    public String uploadFile(MultipartFile file) throws Exception{
        return uploadFile(file, defaultBucketName);
    }

    /**
     * 下载文件
     * 默认桶
     */
    @Override
    public InputStream downloadFile(String objectName){
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(defaultBucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 下载文件
     * 指定桶
     */
    @Override
    public InputStream downloadFile(String bucketName, String objectName){
        try {
            return minioClient.getObject(GetObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build());
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * 删除文件
     */
    @Override
    public void deleteFile(String bucketName, String objectName) throws Exception {
        minioClient.removeObject(RemoveObjectArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .build()
        );
    }

    /**
     * 对外分析url
     */
    @Override
    public String getPresignedObjectUrl(String bucketName, String objectName, int day) throws Exception{
        return minioClient.getPresignedObjectUrl(GetPresignedObjectUrlArgs.builder()
                .bucket(bucketName)
                .object(objectName)
                .expiry(day, TimeUnit.DAYS)
                .build());
    }
}
