package com.joonsang.sample.springbootredis.dao;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ImmutableMap;
import com.joonsang.sample.springbootredis.domain.User;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.text.StringSubstitutor;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

/**
 * Redis DAO
 * @author debugrammer
 * @version 1.0
 * @since 2019-11-10
 */
@Repository
public class RedisDAO {

    private static final String BLOCKED_USER_KEY = "CACHES:BLOCKED_USERS:${USERNAME}";

    private static final String USER_KEY = "USERS:${USERNAME}";

    private final RedisConnectionFactory redisConnectionFactory;

    private final StringRedisTemplate stringRedisTemplate;

    private final RedisTemplate<String, byte[]> messagePackRedisTemplate;

    private final ObjectMapper messagePackObjectMapper;

    public RedisDAO(
            @Qualifier("redisConnectionFactory") RedisConnectionFactory redisConnectionFactory,
            @Qualifier("stringRedisTemplate") StringRedisTemplate stringRedisTemplate,
            @Qualifier("messagePackRedisTemplate") RedisTemplate<String, byte[]> messagePackRedisTemplate,
            @Qualifier("messagePackObjectMapper") ObjectMapper messagePackObjectMapper
    ) {

        this.redisConnectionFactory = redisConnectionFactory;
        this.stringRedisTemplate = stringRedisTemplate;
        this.messagePackRedisTemplate = messagePackRedisTemplate;
        this.messagePackObjectMapper = messagePackObjectMapper;
    }

    public boolean isUserBlocked(String username) {
        String key = StringSubstitutor.replace(
                BLOCKED_USER_KEY,
                ImmutableMap.of("USERNAME", username)
        );

        Boolean hasKey = stringRedisTemplate.hasKey(key);

        return Objects.requireNonNullElse(hasKey, false);
    }

    public long getUserBlockedSecondsLeft(String username) {
        String key = StringSubstitutor.replace(
                BLOCKED_USER_KEY,
                ImmutableMap.of("USERNAME", username)
        );

        Long secondsLeft = stringRedisTemplate.getExpire(key, TimeUnit.SECONDS);

        return Objects.requireNonNullElse(secondsLeft, 0L);
    }

    public void setUserBlocked(String username) {
        String key = StringSubstitutor.replace(
                BLOCKED_USER_KEY,
                ImmutableMap.of("USERNAME", username)
        );

        stringRedisTemplate.opsForValue().set(key, StringUtils.EMPTY, 5, TimeUnit.MINUTES);
    }

    public void deleteUserBlocked(String username) {
        String key = StringSubstitutor.replace(
                BLOCKED_USER_KEY,
                ImmutableMap.of("USERNAME", username)
        );

        stringRedisTemplate.delete(key);
    }

    public User getUser(String username) throws IOException {
        String key = StringSubstitutor.replace(
                USER_KEY, ImmutableMap.of("USERNAME", username)
        );

        byte[] message = messagePackRedisTemplate.opsForValue().get(key);

        if (message == null) {
            return null;
        }

        return messagePackObjectMapper.readValue(message, User.class);
    }

    public void setUser(User user) throws JsonProcessingException {
        String key = StringSubstitutor.replace(
                USER_KEY, ImmutableMap.of("USERNAME", user.getUsername())
        );

        byte[] message = messagePackObjectMapper.writeValueAsBytes(user);

        messagePackRedisTemplate.opsForValue().set(key, message, 1, TimeUnit.HOURS);
    }

    public void deleteUser(String username) {
        String key = StringSubstitutor.replace(
                USER_KEY, ImmutableMap.of("USERNAME", username)
        );

        messagePackRedisTemplate.delete(key);
    }

    public List<String> getAllUsers() {
        String key = StringSubstitutor.replace(USER_KEY, ImmutableMap.of(
                "USERNAME", "*"
        ));

        RedisConnection redisConnection = redisConnectionFactory.getConnection();
        ScanOptions options = ScanOptions.scanOptions().count(50).match(key).build();

        List<String> users = new ArrayList<>();
        Cursor<byte[]> cursor = redisConnection.scan(options);

        while (cursor.hasNext()) {
            String user = StringUtils.replace(new String(cursor.next()), "USERS:", "");

            users.add(user);
        }

        return users;
    }
}
