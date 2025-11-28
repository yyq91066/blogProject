package com.example.blog.service;

import com.example.blog.mapper.PostMapper;
import com.example.blog.model.Logs;
import com.example.blog.model.Post;
import org.hibernate.query.criteria.internal.expression.function.CurrentTimeFunction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class PostService {
    
    @Autowired
    private PostMapper postMapper;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private LogsService logsServiece;
    
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
            redisTemplate.opsForValue().set("time", "redis缓存当前系统时间"+LocalDateTime.now());

            //post.setCreatedTime(LocalDateTime.now());
            //post.setUpdatedTime(LocalDateTime.now());
            Logs logs = new Logs();
            logs.setLogName("PostService-save");
            logs.setLogDescribe("新增文章"+redisTemplate.opsForValue().get("time"));
            logs.setTime(LocalDateTime.now());

            logsServiece.insertLog(logs);
            //System.out.println(insertSuccess);
            postMapper.insert(post);
        } else {
            post.setUpdatedTime(LocalDateTime.now());
            postMapper.update(post);
        }
    }
    
    public boolean deleteById(Long id) {
        return postMapper.deleteById(id);
    }
}