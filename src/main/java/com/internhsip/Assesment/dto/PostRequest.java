package com.internhsip.Assesment.dto;


import lombok.Data;

@Data
public class PostRequest {
    private Long authorId;
    private String authorType;
    private String content;
}