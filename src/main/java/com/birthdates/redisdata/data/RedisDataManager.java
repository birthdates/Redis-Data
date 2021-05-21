package com.birthdates.redisdata.data;

import com.birthdates.redisdata.data.impl.RedisDocument;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

public class RedisDataManager<V extends RedisDocument> {

    @Getter
    private final Map<String, V> data = new HashMap<>();

    public void addData(V value) {
        addData(value, true);
    }

    public void addData(V value, boolean load) {
        data.put(value.getId(), value);
        if (load)
            value.load();
    }

    public boolean removeData(String key) {
        return removeData(key, true);
    }

    public boolean removeData(String key, boolean save) {
        V value = data.remove(key);
        if (value == null) return false;
        if (save) {
            value.save();
        }
        return true;
    }

    public void saveAll() {
        data.forEach((key, value) -> value.save());
    }
}
