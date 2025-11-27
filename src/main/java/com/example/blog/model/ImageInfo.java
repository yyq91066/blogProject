package com.example.blog.model;
 // 按你的项目包路径调整

import java.util.Date;
/**
 * 图片元数据实体类（对应数据库 image_info 表）
 */
// Lombok 注解：自动生成 getter/setter/toString/构造方法等
public class ImageInfo {

    /**
     * 主键ID（自增）
     */
    private Long id;

    /**
     * MinIO中唯一文件名（如：4a04d80b-e328-4822-b3f5-ee8aa73ed2ef.png）
     */
    private String imageId;

    /**
     * 图片原始文件名（如：风景图.png）
     */
    private String originalName;

    /**
     * 图片访问URL（MinIO预签名URL或永久公开URL）
     */
    private String imageUrl;

    /**
     * 图片大小（字节）
     */
    private Long imageSize;

    /**
     * 图片后缀（如：png、jpg、jpeg、webp）
     */
    private String imageSuffix;

    /**
     * 关联业务类型（枚举值：blog_cover=博客封面、user_avatar=用户头像、goods_img=商品图片）
     */
    private String businessType;

    /**
     * 关联业务ID（如博客ID、用户ID、商品ID）
     */
    private String businessId;

    /**
     * 上传时间（默认当前时间）
     */
    private Date uploadTime;
    /**
     * 构造方法
     */
    public ImageInfo() {}

    public ImageInfo(Long id, String imageId, String originalName, String imageUrl, Long imageSize, String imageSuffix, String businessType, String businessId, Date uploadTime) {
        this.id = id;
        this.imageId = imageId;
        this.originalName = originalName;
        this.imageUrl = imageUrl;
        this.imageSize = imageSize;
        this.imageSuffix = imageSuffix;
        this.businessType = businessType;
        this.businessId = businessId;
        this.uploadTime = uploadTime;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getImageId() {
        return imageId;
    }

    public void setImageId(String imageId) {
        this.imageId = imageId;
    }

    public String getOriginalName() {
        return originalName;
    }

    public void setOriginalName(String originalName) {
        this.originalName = originalName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public Long getImageSize() {
        return imageSize;
    }

    public void setImageSize(Long imageSize) {
        this.imageSize = imageSize;
    }

    public String getImageSuffix() {
        return imageSuffix;
    }

    public void setImageSuffix(String imageSuffix) {
        this.imageSuffix = imageSuffix;
    }

    public String getBusinessType() {
        return businessType;
    }

    public void setBusinessType(String businessType) {
        this.businessType = businessType;
    }

    public String getBusinessId() {
        return businessId;
    }

    public void setBusinessId(String businessId) {
        this.businessId = businessId;
    }

    public Date getUploadTime() {
        return uploadTime;
    }

    public void setUploadTime(Date uploadTime) {
        this.uploadTime = uploadTime;
    }
}
