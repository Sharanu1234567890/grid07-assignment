package com.internhsip.Assesment.dto;


import lombok.Data;

@Data
public class CommentRequest {
    private Long authorId;
    private String authorType;
    private String content;
    private int depthLevel;
    private Long botId;
    private Long targetUserId;
}