package com.example.blog.mapper;

import com.example.blog.model.ImageInfo;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import java.util.List;

@Mapper
public interface ImageInfoMapper {
    // 新增图片元数据
    int insert(ImageInfo imageInfo);

    // 按业务类型+业务ID查询图片列表
    List<ImageInfo> selectByBusiness(
            @Param("businessType") String businessType,
            @Param("businessId") String businessId
    );

    // 按imageId删除元数据
    int deleteByImageId(@Param("imageId") String imageId);

    // 按imageId查询元数据
    ImageInfo selectByImageId(@Param("imageId") String imageId);
}