package com.example.blog.service;

import com.example.blog.model.ImageInfo;
import com.example.blog.mapper.ImageInfoMapper;
import com.example.blog.util.MinioUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.UUID;

@Service
public class ImageService {

    @Autowired
    private MinioUtil minIOUtil;

    @Autowired
    private ImageInfoMapper imageInfoMapper;

    /**
     * 上传图片（MinIO存储 + 数据库存元数据）
     */
    public ImageInfo uploadImage(MultipartFile file, String businessType, String businessId) throws Exception {
        // 1. 处理文件名和后缀（避免中文/特殊字符问题）
        String originalName = file.getOriginalFilename();
        if (originalName == null || !originalName.contains(".")) {
            throw new RuntimeException("文件格式不合法，需带后缀（如.png/.jpg）");
        }
        // 提取后缀（如：png、jpg，去掉前面的"."）
        String suffix = originalName.substring(originalName.lastIndexOf(".") + 1).toLowerCase();
        // 生成MinIO唯一文件名（UUID + 后缀，避免重名覆盖）
        String minioFileName = UUID.randomUUID().toString() + "." + suffix;

        // 2. 上传文件到MinIO
        minIOUtil.uploadFile(file);

        // 3. 生成图片访问URL（预签名7天有效期）
        String imageUrl = minIOUtil.getFileUrl(minioFileName);

        // 4. 保存图片元数据到MySQL
        ImageInfo imageInfo = new ImageInfo();
        imageInfo.setImageId(minioFileName); // MinIO唯一文件名（用于删除/查询）
        imageInfo.setOriginalName(originalName); // 原始文件名
        imageInfo.setImageUrl(imageUrl); // 访问URL
        imageInfo.setImageSize(file.getSize()); // 文件大小（字节）
        imageInfo.setImageSuffix(suffix); // 文件后缀
        imageInfo.setBusinessType(businessType); // 关联业务类型
        imageInfo.setBusinessId(businessId); // 关联业务ID
        imageInfo.setUploadTime(new Date()); // 上传时间
        imageInfoMapper.insert(imageInfo);

        return imageInfo;
    }

    /**
     * 按业务类型+业务ID查询图片列表（如：查询博客123的所有封面图）
     */
    public List<ImageInfo> getImageList(String businessType, String businessId) {
        return imageInfoMapper.selectByBusiness(businessType, businessId);
    }

    /**
     * 根据imageId查询图片元数据（Controller的getImageUrl接口依赖）
     */
    public ImageInfo getImageByImageId(String imageId) {
        return imageInfoMapper.selectByImageId(imageId);
    }

    /**
     * 删除图片（同步删除MinIO文件 + 数据库元数据）
     */
    public boolean deleteImage(String imageId) {
        try {
            // 1. 先查询元数据，确认图片存在
            ImageInfo imageInfo = imageInfoMapper.selectByImageId(imageId);
            if (imageInfo == null) {
                return false; // 图片不存在，删除失败
            }

            // 2. 先删除MinIO中的文件（若MinIO删除失败，数据库不删，保证数据一致性）
            minIOUtil.deleteFile(imageId);

            // 3. 再删除数据库中的元数据
            int deleteRows = imageInfoMapper.deleteByImageId(imageId);
            return deleteRows > 0; // 删除成功返回true，失败返回false

        } catch (Exception e) {
            e.printStackTrace();
            // 若MinIO删除成功但数据库删除失败，可根据业务需求处理（如记录日志后续人工清理）
            return false;
        }
    }
}