package com.orbyfied.slate.util.data;

public interface ValueContext {

    ValueContext set(String key, Object val);
    ValueContext put(Object key, Object val);
    <V> V find(String key);
    <V> V find(String key, Class<V> vClass);
    <V> V get(Object key);
    <V> V get(Object key, Class<V> vClass);
    boolean contains(Object key);
    <V> V getOrDefault(Object key, V def);
    <V> V findOrDefault(String key, V def);

}
