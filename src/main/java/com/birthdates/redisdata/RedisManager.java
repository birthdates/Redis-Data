package com.birthdates.redisdata;

import com.birthdates.redisdata.redis.RedisImplementation;
import com.google.gson.Gson;
import lombok.Getter;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

public class RedisManager {

    private static RedisManager instance;
    private final JedisPool jedisPool;

    @Getter
    private final Gson gson;

    public RedisManager() {
        this(null, "localhost", 6379, 0, null, null);
    }

    public RedisManager(JedisPoolConfig poolConfig, String host, int port, int database, String username, String password) {
        if (username == null || password == null) {
            jedisPool = new JedisPool(poolConfig == null ? defaultPoolConfig() : poolConfig, host, port, 2000, null, database);
        } else
            jedisPool = new JedisPool(poolConfig == null ? defaultPoolConfig() : poolConfig, host, port, 2000, username, password, database);
        gson = new Gson();
    }

    public static void init() {
        init(null, "localhost", 0, 6379, null, null);
    }

    public static void init(JedisPoolConfig jedisPoolConfig, String host, int database, int port, String username, String password) {
        if (instance != null) {
            throw new IllegalStateException("RedisManager already initialized!");
        }
        instance = new RedisManager(jedisPoolConfig, host, port, database, username, password);
    }

    public static RedisManager getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RedisManager not setup!");
        }
        return instance;
    }

    public void destroy() {
        for (RedisImplementation pooledImplementation : RedisImplementation.getPooledImplementations()) {
            pooledImplementation.close();
        }
        jedisPool.destroy();
    }

    public RedisImplementation getJedis() {
        Jedis jedis = jedisPool.getResource();
        if (jedis == null) {
            throw new IllegalStateException("Failed to get resource.");
        }
        return RedisImplementation.get(jedis);
    }

    private JedisPoolConfig defaultPoolConfig() {
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
