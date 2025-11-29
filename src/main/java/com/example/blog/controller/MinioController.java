package com.example.blog.controller;

import com.example.blog.model.ImageInfo;
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
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/minio")
public class MinioController {

    @Autowired
    private MinioUtil minioUtil;
//    @Autowired
//    private ImageInfoMapper imageInfoMapper;


//    @GetMapping("/list")
//    public ResponseEntity<List<ImageInfo>> getImageList(
//            @RequestParam("businessType") String businessType,
//            @RequestParam("businessId") String businessId
//    ) {
//        // 按业务类型+业务ID查询图片（如查商品123的所有图片：businessType=goods_img，businessId=123）
//        List<ImageInfo> imageList = imageInfoMapper.selectByBusiness(businessType, businessId);
//        List<ImageInfo> voList = imageList.stream().map(info -> {
//            ImageInfo vo = new ImageInfo();
//            vo.setId(info.getId());
//            vo.setImageUrl(info.getImageUrl());
//            vo.setOriginalName(info.getOriginalName());
//            return vo;
//        }).collect(Collectors.toList());
//        return new ResponseEntity<>(voList, HttpStatus.OK);
//    }
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
            //  1. 调用 MinIO 工具类，从 MinIO 获取文件输入流
            InputStream inputStream = minioUtil.downloadFile(fileName);
            //byte[] bytes = inputStream.readAllBytes();这是jdk9的
            // 2. 读取文件流为字节数组（兼容 JDK 8+）
            byte[] bytes = StreamUtils.copyToByteArray(inputStream);

            // 3. 配置响应头：告知浏览器以“下载”形式处理文件，支持中文文件名
            HttpHeaders headers = new HttpHeaders();
            // 设置文件类型为二进制流（通用类型，浏览器会自动识别文件格式）
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            // 配置下载响应头：attachment 表示“下载”，filename 为下载后的文件名（中文需 UTF-8 编码）
            headers.setContentDispositionFormData("attachment", URLEncoder.encode(fileName, "UTF-8"));
            // 4. 返回响应：文件字节数组 + 响应头 + 200 成功状态码
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
            return "文件删除成功！"+fileName;
        } catch (Exception e) {
            return "删除失败：" + e.getMessage();
        }
    }
}