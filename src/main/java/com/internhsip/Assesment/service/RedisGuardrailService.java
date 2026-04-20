package com.internhsip.Assesment.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisGuardrailService {

    private final StringRedisTemplate redis;

    // lua script so the incr + check + rollback is one atomic redis operation
    // this is the only way to guarantee exactly 100 under concurrent load
    private static final String LUA_INCR_WITH_CAP =
            "local val = redis.call('INCR', KEYS[1]) " +
                    "if val > tonumber(ARGV[1]) then " +
                    "  redis.call('DECR', KEYS[1]) " +
                    "  return -1 " +
                    "end " +
                    "return val";

    public void runBotChecks(Long postId, Long botId, Long userId, int depth) {

        // depth check - no redis needed, just math
        if (depth > 20) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "comment thread too deep, max is 20 levels");
        }

        // cooldown check - bot can't hit same user twice in 10 min
        String cooldownKey = "cooldown:bot_" + botId + ":user_" + userId;
        if (Boolean.TRUE.equals(redis.hasKey(cooldownKey))) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "bot " + botId + " already replied to user " + userId + " recently, wait 10 mins");
        }

        // horizontal cap - max 100 bot replies per post
        // using lua so no race condition possible
        String botCountKey = "post:" + postId + ":bot_count";

        DefaultRedisScript<Long> script = new DefaultRedisScript<>();
        script.setScriptText(LUA_INCR_WITH_CAP);
        script.setResultType(Long.class);

        Long result = redis.execute(script, List.of(botCountKey), "100");

        if (result == null || result == -1L) {
            throw new ResponseStatusException(HttpStatus.TOO_MANY_REQUESTS,
                    "post " + postId + " already has 100 bot replies");
        }

        log.info("bot {} passed all checks for post {}, bot_count is now {}", botId, postId, result);

        // all good - set cooldown so bot cant spam same user again
        redis.opsForValue().set(cooldownKey, "1", Duration.ofMinutes(10));
    }

    public void addViralityPoints(Long postId, String interactionType) {
        int points = switch (interactionType) {
            case "BOT_REPLY"      -> 1;
            case "HUMAN_LIKE"     -> 20;
            case "HUMAN_COMMENT"  -> 50;
            default -> 0;
        };

        if (points > 0) {
            redis.opsForValue().increment("post:" + postId + ":virality_score", points);
        }
    }

    public long getViralityScore(Long postId) {
        String val = redis.opsForValue().get("post:" + postId + ":virality_score");
        return val == null ? 0 : Long.parseLong(val);
    }

    public long getBotCount(Long postId) {
        String val = redis.opsForValue().get("post:" + postId + ":bot_count");
        return val == null ? 0 : Long.parseLong(val);
    }
}