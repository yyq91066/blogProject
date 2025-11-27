package com.example.blog.util;

import cn.hutool.core.io.FileUtil;
import io.minio.*;
import io.minio.http.Method;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Component
public class MinioUtil {

    @Autowired
    private MinioClient minioClient;

    @Value("${minio.bucket}")
    private String defaultBucket;

    /**
     * 上传文件（默认存储到配置的 bucket）
     * @param file 上传的文件（MultipartFile）
     * @return 文件在 MinIO 中的存储路径（key）
     */
    public String uploadFile(MultipartFile file) throws Exception {
        // 生成唯一文件名（避免重复）：UUID + 原文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalFilename);
        String fileName = UUID.randomUUID() + "." + suffix;

        // 上传文件到 MinIO
        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(defaultBucket)
                        .object(fileName)  // MinIO 中存储的文件名（key）
                        .stream(file.getInputStream(), file.getSize(), -1)  // 文件流
                        .contentType(file.getContentType())  // 文件类型
                        .build()
        );

        return fileName;  // 返回存储路径，用于后续下载/访问
    }

    /**
     * 下载文件（根据存储路径获取文件流）
     * @param fileName MinIO 中的存储文件名（uploadFile 方法返回的结果）
     * @return 文件输入流
     */
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(defaultBucket)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 删除文件
     * @param fileName MinIO 中的存储文件名
     */
    public void deleteFile(String fileName) throws Exception {
        minioClient.removeObject(
                RemoveObjectArgs.builder()
                        .bucket(defaultBucket)
                        .object(fileName)
                        .build()
        );
    }

    /**
     * 获取文件临时访问 URL（默认 7 天有效）
     * @param fileName MinIO 中的存储文件名
     * @return 临时访问 URL
     */
    public String getFileUrl(String fileName) throws Exception {
        return minioClient.getPresignedObjectUrl(
                GetPresignedObjectUrlArgs.builder()
                        .bucket(defaultBucket)
                        .object(fileName)
                        .method(Method.GET)
                        .expiry(7, TimeUnit.DAYS)  // 有效期可调整
                        .build()
        );
    }
}