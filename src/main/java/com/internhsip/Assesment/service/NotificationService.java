package com.internhsip.Assesment.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final StringRedisTemplate redis;

    public void handleBotReply(Long userId, Long botId) {
        String cooldownKey = "notif_cooldown:user_" + userId;
        String pendingKey  = "user:" + userId + ":pending_notifs";
        String msg         = "Bot " + botId + " replied to your post";

        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) {
            // user already got a notification recently, queue it instead
            redis.opsForList().rightPush(pendingKey, msg);
            log.info("queued notification for user {}: {}", userId, msg);
        } else {
            // first notification in a while, send it and start the 15 min window
            log.info("PUSH SENT to user {}: {}", userId, msg);
            redis.opsForValue().set(cooldownKey, "1", Duration.ofMinutes(15));
        }
    }
}