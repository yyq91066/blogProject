CREATE TABLE `image_info` (
                              `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
                              `image_id` varchar(64) NOT NULL COMMENT 'MinIO中唯一文件名（如：4a04d80b-e328-4822-b3f5-ee8aa73ed2ef.png）',
                              `original_name` varchar(255) NOT NULL COMMENT '图片原始文件名（如：风景图.png）',
                              `image_url` varchar(512) NOT NULL COMMENT '图片访问URL（MinIO预签名URL或永久URL）',
                              `image_size` bigint(20) DEFAULT NULL COMMENT '图片大小（字节）',
                              `image_suffix` varchar(16) DEFAULT NULL COMMENT '图片后缀（如：png、jpg）',
                              `business_type` varchar(32) NOT NULL COMMENT '关联业务类型（blog_cover：博客封面、user_avatar：用户头像、goods_img：商品图片）',
                              `business_id` varchar(64) NOT NULL COMMENT '关联业务ID（如博客ID、用户ID、商品ID）',
                              `upload_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
                              PRIMARY KEY (`id`),
                              UNIQUE KEY `uk_image_id` (`image_id`),
                              KEY `idx_business` (`business_type`,`business_id`) -- 按业务查询图片（如查商品123的所有图片）
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='图片元数据表';