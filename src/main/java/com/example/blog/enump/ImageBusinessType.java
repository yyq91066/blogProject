package com.example.blog.enump;

public enum ImageBusinessType {
    BLOG_COVER("blog_cover", "博客封面"),
    USER_AVATAR("user_avatar", "用户头像"),
    GOODS_IMG("goods_img", "商品图片"),
    ARTICLE_IMG("article_img", "文章配图");

    private final String code;
    private final String desc;

    ImageBusinessType(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public static ImageBusinessType getByCode(String code) {
        for (ImageBusinessType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        return null;
    }
}