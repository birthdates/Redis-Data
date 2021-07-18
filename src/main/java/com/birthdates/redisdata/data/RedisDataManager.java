package com.birthdates.redisdata.data;

import com.birthdates.redisdata.RedisManager;
import com.birthdates.redisdata.data.impl.RedisDocument;
import com.birthdates.redisdata.redis.RedisImplementation;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.locks.ReentrantLock;

public class RedisDataManager<V extends RedisDocument> {

    @Getter
    private final Map<String, V> data = new HashMap<>();
    protected final ReentrantLock dataLock = new ReentrantLock();

    public void addData(V value) {
        addData(value, true);
    }

    public void addData(V value, boolean load) {
        dataLock.lock();
        try {
            data.put(value.getId(), value);
        } finally {
            dataLock.unlock();
        }
        if (load)
            value.load();
    }

    public boolean removeData(String key) {
        return removeData(key, true);
    }

    public void loadAll(String namespace, Constructor<V> constructorWithId) {
        Set<String> members;
        try (RedisImplementation implementation = RedisManager.getInstance().getJedis()) {
            members = implementation.getJedis().smembers(namespace);
        }

        for (String member : members) {
            V value;
            try {
                value = constructorWithId.newInstance(member);
            } catch(IllegalAccessException | InvocationTargetException | InstantiationException exception) {
                exception.printStackTrace();
                break;
            }

            addData(value);
        }
    }

    public boolean removeData(String key, boolean save) {
        dataLock.lock();
        V value;
        try {
            value =  data.remove(key);
        } finally {
            dataLock.unlock();
        }

        if (value == null) return false;
        if (save) {
            value.save();
        }
        return true;
    }

    public void saveAll() {
        dataLock.lock();
        try {
            data.forEach((key, value) -> value.save());
        } finally {
            dataLock.unlock();
        }
    }
}
