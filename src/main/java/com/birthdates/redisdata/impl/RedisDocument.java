package com.birthdates.redisdata.impl;

import com.birthdates.redisdata.RedisManager;
import com.birthdates.redisdata.redis.RedisImplementation;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

public abstract class RedisDocument {


    public abstract void onLoaded();

    public abstract String getNamespace();

    public abstract String getId();

    public abstract Class<?> getType();

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
        Map<String, String> values = save ? null : implementation.getJedis().hgetAll(getId());
        List<Field> fields = new ArrayList<>();
        getAllFields(fields, getType());

        for (Field field : fields) {
            if (Modifier.isTransient(field.getModifiers())) continue;

            Object value;
            try {
                value = save ? field.get(this) : values.remove(field.getName());
            } catch (IllegalAccessException exception) {
                exception.printStackTrace();
                continue;
            }
            if (value == null) continue;

            Class<?> fieldType = field.getType();
            if (value instanceof Enum<?>) {
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

            if (!fieldType.equals(String.class)) {
                value = RedisManager.getInstance().getGson().fromJson(value.toString(), fieldType);
            }
            field.setAccessible(true);
            if (!save) {
                try {
                    field.set(this, value);
                } catch (IllegalAccessException exception) {
                    exception.printStackTrace();
                }
                continue;
            }
            implementation.getJedis().hset(getNamespace() + ":" + getId(), field.getName(), RedisManager.getInstance().getGson().toJson(value));
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
