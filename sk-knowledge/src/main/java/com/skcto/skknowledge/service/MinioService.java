package com.skcto.skknowledge.service;

import io.minio.errors.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

public interface MinioService {
    boolean bucketExists(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    void createBucket(String bucketName) throws ServerException, InsufficientDataException, ErrorResponseException, IOException, NoSuchAlgorithmException, InvalidKeyException, InvalidResponseException, XmlParserException, InternalException;

    String uploadFile(MultipartFile file, String bucketName) throws Exception;

    String uploadFile(MultipartFile file) throws Exception;

    InputStream downloadFile(String objectName);

    InputStream downloadFile(String bucketName, String objectName);

    void deleteFile(String buckName, String objectName) throws Exception;

    String getPresignedObjectUrl(String bucketName, String objectName, int day) throws Exception;
}
