package com.birthdates.redisdata;

import com.birthdates.redisdata.data.RedisDataManager;
import com.birthdates.redisdata.data.impl.RedisDocument;

public class RedisDataTest {

    public RedisDataTest() {
        RedisManager.init();

        long millis = System.currentTimeMillis();
        RedisDataManager<TestDocument> redisDataManager = new RedisDataManager<>();

        redisDataManager.addData(new TestDocument());
        //redisDataManager.saveAll();
        throw new IllegalStateException((System.currentTimeMillis()-millis) + "ms");
    }

    private static class TestDocument extends RedisDocument {

        @Override
        public void onLoaded() { }

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
