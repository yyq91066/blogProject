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
     * 1.MultipartFile是Spring 框架提供的一个接口，
     * 专门用于 接收 HTTP 请求中上传的文件（比如你用
     * Postman 上传的图片、文档等），是 Spring Boot 中处理文件上传的核心工具。
     * 2.当你通过浏览器 / Postman 上传文件时，HTTP 请求会把文件以
     * multipart/form-data 格式打包发送；MultipartFile
     * 就是 Spring 帮你 “拆包” 后得到的 文件对象—— 你可以通过它获取
     * 文件名、文件内容、文件大小等信息，再进行后续的存储（比如上传到 MinIO）
     */
    public String uploadFile(MultipartFile file) throws Exception {
        // 1.生成唯一文件名（避免重复）：UUID + 原文件后缀
        String originalFilename = file.getOriginalFilename();
        String suffix = FileUtil.extName(originalFilename);
        String fileName = UUID.randomUUID() + "." + suffix;
        /**
         * 1.FileUtil：Hutool 库中的文件操作工具类，封装了大量简化文件处理的静态方法
         （如获取后缀、创建文件、复制文件等）；
         2.extName(originalFilename)：FileUtil 的静态方法，
         专门用于提取文件名的后缀，参数是文件的原始名称（如 20251126-193120.png）。
         */

        // 2.上传文件到 MinIO
        minioClient.putObject(
                PutObjectArgs.builder() // 构建上传请求参数（存储桶、文件名、文件流等
                        .bucket(defaultBucket) // 指定存储桶
                        .object(fileName)  // MinIO 中存储的文件名（key）
                        .stream(file.getInputStream(), file.getSize(), -1)  // 文件流
                        .contentType(file.getContentType())  // 文件类型
                        .build() //将上面配置的所有参数，构建成一个 PutObjectArgs 对象
        );
        /**
         * 第一个参数 file.getInputStream()：从 MultipartFile
         * 中获取的文件输入流（即上传文件的原始内容），MinIO 会读取这个流来写入文件；
         * 第二个参数 file.getSize()：文件的大小（单位：字节），由 MultipartFile
         * 提供，告诉 MinIO 预期要接收的文件总大小；
         * 第三个参数 -1：表示「不分片上传」（默认行为），适用于小文件（通常小于 5GB）；
         * 如果是大文件（超过 5GB），需要指定分片大小（比如 1024*1024*5 表示 5MB 分片），
         * MinIO 会自动分片上传。
         */

        return fileName;  // 返回存储路径，用于后续下载/访问
    }

    /**
     * 下载文件（根据存储路径获取文件流）
     * @param fileName MinIO 中的存储文件名（uploadFile 方法返回的结果）
     * @return 文件输入流
     */
    public InputStream downloadFile(String fileName) throws Exception {
        return minioClient.getObject(
                GetObjectArgs.builder() //// 构建下载请求参数（指定存储桶和文件名
                        .bucket(defaultBucket)  // 从哪个 MinIO「存储桶（Bucket）」中下载文件
                        .object(fileName)  //要下载的文件在 MinIO 中的「唯一标识（Object Key）」
                        .build() //存储桶和文件名参数构建成 GetObjectArgs
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
                        .bucket(defaultBucket) // 存储桶
                        .object(fileName) // 要访问的文件名
                        .method(Method.GET)  // 指定请求方法
                        .expiry(7, TimeUnit.DAYS)  // 有效期
                        .build()
        );
    }
}