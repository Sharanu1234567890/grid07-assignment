package com.internhsip.Assesment.controller;


import com.internhsip.Assesment.dto.CommentRequest;
import com.internhsip.Assesment.dto.LikeRequest;
import com.internhsip.Assesment.dto.PostRequest;
import com.internhsip.Assesment.entity.Comment;
import com.internhsip.Assesment.entity.Post;
import com.internhsip.Assesment.service.PostService;
import com.internhsip.Assesment.service.RedisGuardrailService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final RedisGuardrailService guardrail;

    @PostMapping("/posts")
    @ResponseStatus(HttpStatus.CREATED)
    public Post createPost(@RequestBody PostRequest req) {
        return postService.createPost(req);
    }

    @GetMapping("/posts/{postId}")
    public Post getPost(@PathVariable Long postId) {
        return postService.getPost(postId);
    }

    @PostMapping("/posts/{postId}/comments")
    @ResponseStatus(HttpStatus.CREATED)
    public Comment addComment(@PathVariable Long postId,
                              @RequestBody CommentRequest req) {
        return postService.addComment(postId, req);
    }

    @GetMapping("/posts/{postId}/comments")
    public List<Comment> getComments(@PathVariable Long postId) {
        return postService.getComments(postId);
    }

    @PostMapping("/posts/{postId}/like")
    public String likePost(@PathVariable Long postId,
                           @RequestBody LikeRequest req) {
        return postService.likePost(postId, req);
    }

    // handy for testing/debugging redis state
    @GetMapping("/posts/{postId}/stats")
    public Map<String, Object> stats(@PathVariable Long postId) {
        return Map.of(
                "botCount",      guardrail.getBotCount(postId),
                "viralityScore", guardrail.getViralityScore(postId)
        );
    }
}