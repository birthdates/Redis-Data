package com.birthdates.redisdata;

import com.birthdates.redisdata.redis.RedisImplementation;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class RedisManager {

    private static RedisManager instance;

    private final JedisPool jedisPool;

    public RedisManager() {
        instance = this;
        jedisPool = new JedisPool(getPoolConfig());
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RedisManager not setup!");
        }
        return instance;
    }

    public RedisImplementation getJedis() {
        Jedis jedis = jedisPool.getResource();
        if (jedis == null) {
            throw new IllegalStateException("Failed to get resource.");
        }
        return RedisImplementation.get(jedis);
    }

    private JedisPoolConfig getPoolConfig() {
        final JedisPoolConfig poolConfig = new JedisPoolConfig();
        poolConfig.setMaxTotal(128);
        poolConfig.setMaxIdle(128);
        poolConfig.setMinIdle(16);
        poolConfig.setTestWhileIdle(true);
        poolConfig.setMinEvictableIdleTimeMillis(Duration.ofSeconds(60).toMillis());
        poolConfig.setTimeBetweenEvictionRunsMillis(Duration.ofSeconds(30).toMillis());
        poolConfig.setNumTestsPerEvictionRun(-1);
        return poolConfig;
    }
}
