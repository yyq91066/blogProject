package com.example.blog.controller;

import com.example.blog.model.ImageInfo;
import com.example.blog.enump.ImageBusinessType;
import com.example.blog.service.ImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
public class ImageController {

    @Autowired
    private ImageService imageService;

    /**
     * 图片上传接口
     * @param file 图片文件
     * @param businessType 业务类型（blog_cover/user_avatar/goods_img/article_img）
     * @param businessId 关联业务ID（博客ID/用户ID/商品ID等）
     */
    @PostMapping("/upload")
    public ResponseEntity<Map<String, Object>> upload(
            @RequestParam("file") MultipartFile file,
            @RequestParam("businessType") String businessType,
            @RequestParam("businessId") String businessId
    ) {
        // 初始化返回结果Map
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 1. 校验业务类型是否合法
            if (ImageBusinessType.getByCode(businessType) == null) {
                resultMap.put("success", false);
                resultMap.put("message", "非法业务类型！支持的类型：blog_cover/user_avatar/goods_img/article_img");
                return ResponseEntity.badRequest().body(resultMap);
            }

            // 2. 校验文件合法性
            if (file.isEmpty()) {
                resultMap.put("success", false);
                resultMap.put("message", "文件不能为空！");
                return ResponseEntity.badRequest().body(resultMap);
            }
            // 限制文件大小5MB
            long maxSize = 5 * 1024 * 1024;
            if (file.getSize() > maxSize) {
                resultMap.put("success", false);
                resultMap.put("message", "文件大小不能超过5MB！");
                return ResponseEntity.badRequest().body(resultMap);
            }

            // 3. 调用Service上传图片（MinIO存储+数据库存元数据）
            ImageInfo imageInfo = imageService.uploadImage(file, businessType, businessId);

            // 4. 组装成功响应
            resultMap.put("success", true);
            resultMap.put("message", "图片上传成功！");
            resultMap.put("imageId", imageInfo.getImageId()); // MinIO唯一文件名（用于删除）
            resultMap.put("imageUrl", imageInfo.getImageUrl()); // 图片访问URL（前端直接使用）
            resultMap.put("originalName", imageInfo.getOriginalName()); // 原始文件名
            resultMap.put("imageSize", imageInfo.getImageSize()); // 图片大小（字节）
            resultMap.put("imageSuffix", imageInfo.getImageSuffix()); // 图片后缀

            return ResponseEntity.ok(resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            // 组装失败响应
            resultMap.put("success", false);
            resultMap.put("message", "上传失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
        }
    }

    /**
     * 按业务类型+业务ID查询图片列表（如：查询博客123的所有封面图）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getImageList(
            @RequestParam("businessType") String businessType,
            @RequestParam("businessId") String businessId
    ) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 1. 校验业务类型是否合法
            if (ImageBusinessType.getByCode(businessType) == null) {
                resultMap.put("success", false);
                resultMap.put("message", "非法业务类型！");
                return ResponseEntity.badRequest().body(resultMap);
            }

            // 2. 调用Service查询图片列表
            List<ImageInfo> imageList = imageService.getImageList(businessType, businessId);

            // 3. 组装响应（返回简化后的图片信息，避免敏感字段）
            List<Map<String, Object>> imageVoList = new ArrayList<>();
            for (ImageInfo image : imageList) {
                Map<String, Object> imageVo = new HashMap<>();
                imageVo.put("imageId", image.getImageId());
                imageVo.put("imageUrl", image.getImageUrl());
                imageVo.put("originalName", image.getOriginalName());
                imageVo.put("imageSize", image.getImageSize());
                imageVo.put("uploadTime", image.getUploadTime());
                imageVoList.add(imageVo);
            }

            resultMap.put("success", true);
            resultMap.put("message", "查询成功！");
            resultMap.put("total", imageVoList.size()); // 图片总数
            resultMap.put("imageList", imageVoList); // 图片列表数据

            return ResponseEntity.ok(resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("success", false);
            resultMap.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
        }
    }

    /**
     * 图片删除接口（同步删除MinIO中的文件和数据库元数据）
     * @param imageId MinIO中的唯一文件名（上传接口返回的imageId）
     */
    @DeleteMapping("/delete/{imageId}")
    public ResponseEntity<Map<String, Object>> delete(@PathVariable String imageId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 调用Service删除图片
            boolean deleteSuccess = imageService.deleteImage(imageId);

            if (deleteSuccess) {
                resultMap.put("success", true);
                resultMap.put("message", "图片删除成功！");
                return ResponseEntity.ok(resultMap);
            } else {
                resultMap.put("success", false);
                resultMap.put("message", "图片不存在或删除失败！");
                return ResponseEntity.badRequest().body(resultMap);
            }

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("success", false);
            resultMap.put("message", "删除失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
        }
    }

    /**
     * 单独获取图片访问URL（根据imageId）
     * @param imageId MinIO中的唯一文件名
     */
    @GetMapping("/url/{imageId}")
    public ResponseEntity<Map<String, Object>> getImageUrl(@PathVariable String imageId) {
        Map<String, Object> resultMap = new HashMap<>();
        try {
            // 查询图片元数据
            ImageInfo imageInfo = imageService.getImageByImageId(imageId);
            if (imageInfo == null) {
                resultMap.put("success", false);
                resultMap.put("message", "图片不存在！");
                return ResponseEntity.badRequest().body(resultMap);
            }

            resultMap.put("success", true);
            resultMap.put("imageUrl", imageInfo.getImageUrl());
            resultMap.put("message", "查询成功！");
            return ResponseEntity.ok(resultMap);

        } catch (Exception e) {
            e.printStackTrace();
            resultMap.put("success", false);
            resultMap.put("message", "查询失败：" + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(resultMap);
        }
    }
}