package com.jblue.kv.cache;

public interface BaseKVCache {

    void loadCache();

    default void init() {
        loadCache();
    }

    String getName();
}
