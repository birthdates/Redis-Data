package com.birthdates.redisdata.data.impl;

import com.birthdates.redisdata.RedisManager;
import com.birthdates.redisdata.redis.RedisImplementation;
import lombok.Getter;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class RedisDocument {

    @Getter
    private boolean isNew;

    public void onLoaded() {
    }

    public abstract String getNamespace();

    public abstract String getId();

    public abstract Class<?> getType();

    public String getKey() {
        return getNamespace() + ":" + getId();
    }

    public void load() {
        try (RedisImplementation implementation = RedisManager.getInstance().getJedis()) {
            loopFields(false, implementation);
        } finally {
            onLoaded();
        }
    }

    public void save() {
        try (RedisImplementation implementation = RedisManager.getInstance().getJedis()) {
            loopFields(true, implementation);
        }
    }

    private void loopFields(boolean save, RedisImplementation implementation) {
        Map<String, String> values = save ? null : implementation.getJedis().hgetAll(getKey());
        if (values != null && values.isEmpty()) {
            isNew = true;
            return;
        }
        List<Field> fields = new ArrayList<>();
        getAllFields(fields, getType());

        for (Field field : fields) {
            if (field == null || Modifier.isTransient(field.getModifiers())) continue;

            String fieldName = field.getName();

            field.setAccessible(true);
            Object value;
            try {
                value = values == null ? field.get(this) : values.remove(fieldName);
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
                continue;
            }
            if (value == null) continue;

            Class<?> fieldType = field.getType();
            if (fieldType.isEnum()) {
                if (save) {
                    value = ((Enum<?>) value).name();
                } else {
                    try {
                        value = fieldType.getMethod("valueOf", String.class).invoke(null, value.toString());
                    } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException exception) {
                        exception.printStackTrace();
                        continue;
                    }
                }
            }

            if (!save && !fieldType.equals(String.class)) {
                value = RedisManager.getInstance().getGson().fromJson(value.toString(), fieldType);
            }
            if (!save) {
                try {
                    field.set(this, value);
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
                continue;
            }
            implementation.getJedis().hset(getKey(), fieldName, RedisManager.getInstance().getGson().toJson(value));
        }
    }

    public void delete() {
        try (RedisImplementation implementation = RedisManager.getInstance().getJedis()) {
            implementation.getJedis().del(getKey());
        }
    }

    public void expire(int milliseconds) {
        try (RedisImplementation implementation = RedisManager.getInstance().getJedis()) {
            implementation.getJedis().pexpire(getKey(), milliseconds);
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
