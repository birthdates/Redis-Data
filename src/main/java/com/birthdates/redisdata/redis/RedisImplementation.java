package com.birthdates.redisdata.redis;

import lombok.Getter;
import lombok.Setter;
import redis.clients.jedis.Jedis;

import java.util.ArrayList;
import java.util.List;

public class RedisImplementation implements AutoCloseable {

    @Getter
    private static final List<RedisImplementation> pooledImplementations = new ArrayList<>();

    @Getter
    @Setter
    private Jedis jedis;

    public static RedisImplementation get(Jedis jedis) {
        RedisImplementation implementation;
        if (!pooledImplementations.isEmpty()) {
            implementation = pooledImplementations.remove(0);
        } else implementation = new RedisImplementation();
        implementation.setJedis(jedis);
        return implementation;
    }

    @Override
    public void close() {
        jedis.disconnect();
        jedis.close();

        if (pooledImplementations.isEmpty())
            pooledImplementations.add(this);
    }
}
