package com.birthdates.redisdata.data.impl;

import com.birthdates.redisdata.RedisManager;
import com.birthdates.redisdata.redis.RedisImplementation;
import lombok.Getter;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public abstract class RedisDocument {

    @Getter
    private transient boolean isNew;

    public void onLoaded() {
    }

    public abstract String getNamespace();

    public abstract String getId();

    public abstract Class<?> getType();

    public Object serialize(String fieldName, Object obj) {
        return obj; //return null to skip
    }

    public Object deserialize(String fieldName, Object data) {
        return data; //return null to skip
    }

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
            implementation.getJedis().sadd(getNamespace(), getId());
        }
    }

    public void applyTo(RedisDocument document) {
        List<Field> fields = new ArrayList<>();
        getAllFields(fields, getType()); //get all fields from super classes

        for (Field field : fields) {
            Field otherField;
            try {
                otherField = document.getClass().getDeclaredField(field.getName());
            } catch(NoSuchFieldException ignored) {
                continue;
            }

            if (!field.getType().equals(otherField.getType()))
                continue;

            field.setAccessible(true);
            otherField.setAccessible(true);

            Object value;
            Object otherValue;
            try {
                value = field.get(this);
                otherValue = otherField.get(this);
            } catch(IllegalAccessException exception) {
                exception.printStackTrace();
                continue;
            }

            if (value.equals(otherValue))
                continue;

            if (value instanceof Collection<?>) {
                Collection collection = (Collection<?>) value;
                Collection otherCollection = (Collection<?>) otherValue;
                otherCollection.clear();
                otherCollection.addAll(collection);
                continue;
            }


            try {
                otherField.set(document, value);
            } catch(IllegalAccessException exception) {
                exception.printStackTrace();
            }
        }
    }

    public <T extends RedisDocument> T clone(Class<T> type) {
        Constructor<T> constructor;
        try {
            constructor = type.getConstructor(String.class);
        } catch(NoSuchMethodException exception) {
            exception.printStackTrace();
            return null;
        }
        T newObj;
        try {
            newObj = constructor.newInstance(getId());
        } catch(InstantiationException | IllegalAccessException | InvocationTargetException exception) {
            exception.printStackTrace();
            return null;
        }
        applyTo(newObj);
        return newObj;
    }

    private void loopFields(boolean save, RedisImplementation implementation) {
        Map<String, String> values = save ? null : implementation.getJedis().hgetAll(getKey());
        if (values != null && values.isEmpty()) { //if we're saving & there's no data
            isNew = true;
            return;
        }
        List<Field> fields = new ArrayList<>();
        getAllFields(fields, getType()); //get all fields from super classes

        for (Field field : fields) {
            if (field == null || Modifier.isTransient(field.getModifiers())) continue;

            String fieldName = field.getName();

            field.setAccessible(true);
            Object value;
            try {
                value = values == null ? field.get(this) : values.remove(fieldName); //on save, get field value, else get redis map value
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

            if (!save) {
                value = RedisManager.getInstance().getGson().fromJson(value.toString(), fieldType);
            }

            value = save ? serialize(fieldName, value) : deserialize(fieldName, value);
            if (value == null) continue; //if we've decided to skip this field

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
        String key = getKey();
        String id = getId();
        try (RedisImplementation implementation = RedisManager.getInstance().getJedis()) {
            implementation.getJedis().del(key);
            implementation.getJedis().srem(getNamespace(), id);
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
