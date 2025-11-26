package com.example.blog.controller;

import com.example.blog.model.Post;
import com.example.blog.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.HashMap;
import java.util.Map;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/posts")
@CrossOrigin(origins = "*")
public class PostController {
    
    @Autowired
    private PostService postService;
    
    @GetMapping
    public List<Post> getAllPosts() {
        return postService.findAll();
    }
    
    @GetMapping("/{id}")
    public Post getPostById(@PathVariable Long id) {
        return postService.findById(id);
    }
    
    @PostMapping
    public ResponseEntity<?> createPost(@RequestBody Post post) {
        try {
            if (post.getTitle() == null || post.getTitle().isEmpty()) {
                Map<String, String> error = new HashMap<>();
            error.put("error", "标题不能为空");
            return ResponseEntity.badRequest().body(error);
            }
            if (post.getContent() == null || post.getContent().isEmpty()) {
                Map<String, String> error = new HashMap<>();
            error.put("error", "内容不能为空");
            return ResponseEntity.badRequest().body(error);
            }
            if (post.getAuthor() == null || post.getAuthor().isEmpty()) {
                Map<String, String> error = new HashMap<>();
            error.put("error", "作者不能为空");
            return ResponseEntity.badRequest().body(error);
            }
            post.setCreatedTime(LocalDateTime.now());
            post.setUpdatedTime(LocalDateTime.now());
            postService.save(post);
            return ResponseEntity.ok(post);
        } catch (Exception e) {
            Map<String, String> error = new HashMap<>();
            error.put("error", "发布文章失败: " + e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
        }
    }
    
    @PutMapping("/{id}")
    public Post updatePost(@PathVariable Long id, @RequestBody Post post) {
        post.setId(id);
        postService.save(post);
        return post;
    }
    
    @DeleteMapping("/{id}")
    public void deletePost(@PathVariable Long id) {
        postService.deleteById(id);
    }
}