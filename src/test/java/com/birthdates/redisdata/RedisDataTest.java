package com.birthdates.redisdata;

import com.birthdates.redisdata.data.RedisDataManager;
import com.birthdates.redisdata.data.impl.RedisDocument;

public class RedisDataTest {

    public RedisDataTest() {
        RedisManager.init();

        RedisDataManager<TestDocument> redisDataManager = new RedisDataManager<>();

        TestDocument document = new TestDocument();

        redisDataManager.addData(document);
        redisDataManager.saveAll();
        document.delete();
    }

    private static class TestDocument extends RedisDocument {

        public String name = "test_name";
        public int id = 0;

        @Override
        public void onLoaded() {
        }

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
