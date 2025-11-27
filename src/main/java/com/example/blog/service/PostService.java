package com.example.blog.service;

import com.example.blog.mapper.PostMapper;
import com.example.blog.model.Post;
import org.hibernate.query.criteria.internal.expression.function.CurrentTimeFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;

@Service
public class PostService {
    
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    
    public List<Post> findAll() {
       // Post post = new Post();

        redisTemplate.opsForValue().set("time", "redis缓存当前系统时间"+LocalDateTime.now());
        Object o = redisTemplate.opsForValue().get("time");
        System.out.println(o.toString());
       // List<Post> all = ;
        return postMapper.findAll();
    }
    
    public Post findById(Long id) {

        return postMapper.findById(id);
    }
    
    public void save(Post post) {
        if (post.getId() == null) {
            //post.setCreatedTime(LocalDateTime.now());
            //post.setUpdatedTime(LocalDateTime.now());
            postMapper.insert(post);
        } else {
            post.setUpdatedTime(LocalDateTime.now());
            postMapper.update(post);
        }
    }
    
    public void deleteById(Long id) {
        postMapper.deleteById(id);
    }
}