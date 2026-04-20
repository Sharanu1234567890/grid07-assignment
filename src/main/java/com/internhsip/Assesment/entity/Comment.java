package com.internhsip.Assesment.entity;


import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "comments")
@Getter @Setter
@NoArgsConstructor
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long postId;
    private Long authorId;
    private String authorType; // "USER" or "BOT"

    @Column(columnDefinition = "TEXT")
    private String content;

    private int depthLevel;

    // these two are only relevant when a bot is replying
    private Long botId;
    private Long targetUserId; // the human who owns the post

    @CreationTimestamp
    private LocalDateTime createdAt;
}