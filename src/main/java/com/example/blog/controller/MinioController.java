package com.example.blog.controller;

import com.example.blog.util.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.net.URLEncoder;

@RestController
@RequestMapping("/minio")
public class MinioController {

    @Autowired
    private MinioUtil minioUtil;

    /**
     * 文件上传接口
     */
    @PostMapping("/upload")
    public String upload(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = minioUtil.uploadFile(file);
            return "上传成功！文件存储路径：" + fileName + "\n访问URL：" + minioUtil.getFileUrl(fileName);
        } catch (Exception e) {
            return "上传失败：" + e.getMessage();
        }
    }

    /**
     * 文件下载接口
     */
    @GetMapping("/download/{fileName}")
    public ResponseEntity<byte[]> download(@PathVariable String fileName) {
        try {
            // 获取文件流
            InputStream inputStream = minioUtil.downloadFile(fileName);
            //byte[] bytes = inputStream.readAllBytes();这是jdk9的
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);

            // 设置响应头（支持中文文件名）
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, "UTF-8"));

            return new ResponseEntity<>(bytes, headers, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * 获取文件访问 URL
     */
    @GetMapping("/url/{fileName}")
    public String getFileUrl(@PathVariable String fileName) {
        try {
            return "文件访问URL（7天有效）：" + minioUtil.getFileUrl(fileName);
        } catch (Exception e) {
            return "获取URL失败：" + e.getMessage();
        }
    }

    /**
     * 删除文件
     */
    @DeleteMapping("/delete/{fileName}")
    public String deleteFile(@PathVariable String fileName) {
        try {
            minioUtil.deleteFile(fileName);
            return "文件删除成功！";
        } catch (Exception e) {
            return "删除失败：" + e.getMessage();
        }
    }
}