package com.internhsip.Assesment.service;


import com.internhsip.Assesment.dto.CommentRequest;
import com.internhsip.Assesment.dto.LikeRequest;
import com.internhsip.Assesment.dto.PostRequest;
import com.internhsip.Assesment.entity.Comment;
import com.internhsip.Assesment.entity.Post;
import com.internhsip.Assesment.repository.CommentRepository;
import com.internhsip.Assesment.repository.PostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepo;
    private final CommentRepository commentRepo;
    private final RedisGuardrailService guardrail;
    private final NotificationService notifications;

    @Transactional
    public Post createPost(PostRequest req) {
        Post p = new Post();
        p.setAuthorId(req.getAuthorId());
        p.setAuthorType(req.getAuthorType().toUpperCase());
        p.setContent(req.getContent());
        return postRepo.save(p);
    }

    @Transactional
    public Comment addComment(Long postId, CommentRequest req) {

        // make sure post actually exists
        postRepo.findById(postId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

        boolean isBot = "BOT".equalsIgnoreCase(req.getAuthorType());

        if (isBot) {
            Long botId = req.getBotId() != null ? req.getBotId() : req.getAuthorId();

            // redis checks run BEFORE db write - that's the whole point
            guardrail.runBotChecks(postId, botId, req.getTargetUserId(), req.getDepthLevel());
            guardrail.addViralityPoints(postId, "BOT_REPLY");
            notifications.handleBotReply(req.getTargetUserId(), botId);

        } else {
            guardrail.addViralityPoints(postId, "HUMAN_COMMENT");
        }

        Comment c = new Comment();
        c.setPostId(postId);
        c.setAuthorId(req.getAuthorId());
        c.setAuthorType(req.getAuthorType().toUpperCase());
        c.setContent(req.getContent());
        c.setDepthLevel(req.getDepthLevel());
        c.setBotId(req.getBotId());
        c.setTargetUserId(req.getTargetUserId());

        return commentRepo.save(c);
    }

    @Transactional
    public String likePost(Long postId, LikeRequest req) {
        postRepo.findById(postId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));

        guardrail.addViralityPoints(postId, "HUMAN_LIKE");
        long score = guardrail.getViralityScore(postId);

        log.info("user {} liked post {}, virality now {}", req.getUserId(), postId, score);
        return "liked! virality score is now " + score;
    }

    public Post getPost(Long postId) {
        return postRepo.findById(postId).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "post not found"));
    }

    public List<Comment> getComments(Long postId) {
        return commentRepo.findByPostId(postId);
    }
}