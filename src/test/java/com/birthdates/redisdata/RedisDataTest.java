package com.birthdates.redisdata;

import com.birthdates.redisdata.impl.RedisDocument;

public class RedisDataTest {

    public RedisDataTest() {
        RedisManager.init();

        TestDocument testDocument = new TestDocument();
        testDocument.load();
        testDocument.save();
    }

    private static class TestDocument extends RedisDocument {

        @Override
        public void onLoaded() {
        }

        public String name = "test_name";
        public int id = 0;

        @Override
        public String getNamespace() {
            return "test";
        }

        @Override
        public String getId() {
            return "id";
        }

        @Override
        public Class<?> getType() {
            return TestDocument.class;
        }
    }
}
