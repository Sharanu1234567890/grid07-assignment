package com.internhsip.Assesment.scheduler;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationSweeper {

    private final StringRedisTemplate redis;

    // runs every 5 mins - in prod this would be 15 mins
    @Scheduled(fixedRate = 5 * 60 * 1000)
    public void sweep() {
        log.info("sweeper running...");

        Set<String> keys = redis.keys("user:*:pending_notifs");
        if (keys == null || keys.isEmpty()) {
            log.info("nothing to sweep");
            return;
        }

        for (String key : keys) {
            List<String> msgs = redis.opsForList().range(key, 0, -1);
            if (msgs == null || msgs.isEmpty()) continue;

            String userId = key.replace("user:", "").replace(":pending_notifs", "");
            String first  = msgs.get(0);
            int others    = msgs.size() - 1;

            if (others > 0) {
                log.info("Summarized Push Notification: {} and [{}] others interacted with your posts",
                        first, others);
            } else {
                log.info("Summarized Push Notification: {}", first);
            }

            redis.delete(key);
        }

        log.info("sweep done");
    }
}