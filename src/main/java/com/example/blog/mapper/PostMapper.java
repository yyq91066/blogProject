package com.example.blog.mapper;

import com.example.blog.model.Post;
import org.apache.ibatis.annotations.*;

import java.util.List;

@Mapper
public interface PostMapper {
    
    @Select("SELECT * FROM posts where (del_flag != 1 OR del_flag IS NULL)")
    List<Post> findAll();
    
    @Select("SELECT * FROM posts WHERE id = #{id} and (del_flag != 1 OR del_flag IS NULL)")
    Post findById(Long id);
    
    @Insert("INSERT INTO posts(title, content, author, created_time, updated_time) VALUES(#{title}, #{content}, #{author}, #{createdTime}, #{updatedTime})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(Post post);
    
    @Update("UPDATE posts SET title=#{title}, content=#{content}, author=#{author}, updated_time=#{updatedTime} WHERE id=#{id}")
    void update(Post post);
    
    @Delete("DELETE FROM posts WHERE id = #{id}")
    void deleteById(Long id);
}