package com.birthdates.redisdata.impl;

import com.birthdates.redisdata.RedisManager;
import com.birthdates.redisdata.redis.RedisImplementation;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class RedisDocument {


    public abstract void onLoaded();
    public abstract String getId();
    public abstract Class<?> getType();


    public void load() {
        try (RedisImplementation implementation = RedisManager.getInstance().getJedis()) {
            Map<String, String> values = implementation.getJedis().hgetAll(getId());

            List<Field> fields = new ArrayList<>();
            getAllFields(fields, getType());

            for (Field field : fields) {
                if(Modifier.isTransient(field.getModifiers())) continue;
                String value = values.remove(field.getName());
                if(value == null) continue;

            }
        } finally {
            onLoaded();
        }
    }

    private void getAllFields(List<Field> fields, Class<?> type) {
        fields.addAll(Arrays.asList(type.getDeclaredFields()));

        if (type.getSuperclass() == null) {
            return;
        }
        getAllFields(fields, type.getSuperclass());
    }
}
