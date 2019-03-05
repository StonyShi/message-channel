package com.stony.mc.concurrent;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterator;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>message-channel
 * <p>com.stony.mc.concurrent
 *
 * @author stony
 * @version 上午10:10
 * @since 2019/1/23
 */
public class ConcurrentHashSet<E> extends AbstractSet<E> implements Set<E>, Cloneable, java.io.Serializable{
    transient ConcurrentHashMap<E, Object> map;
    private static final Object PRESENT = new Object();

    public ConcurrentHashSet(int initialCapacity, float loadFactor) {
        this.map = new ConcurrentHashMap<>(initialCapacity, loadFactor);
    }
    public ConcurrentHashSet(int initialCapacity) {
        this.map = new ConcurrentHashMap<>(initialCapacity);
    }
    public ConcurrentHashSet() {
        this(1024);
    }
    public Iterator<E> iterator() {
        return map.keySet().iterator();
    }
    @Override
    public int size() {
        return map.size();
    }
    public boolean isEmpty() {
        return map.isEmpty();
    }
    public boolean contains(Object o) {
        return map.containsKey(o);
    }
    public boolean add(E e) {
        return map.put(e, PRESENT)==null;
    }
    public boolean remove(Object o) {
        return map.remove(o)==PRESENT;
    }
    public void clear() {
        map.clear();
    }
    @SuppressWarnings("unchecked")
    public Object clone() {
        try {
            ConcurrentHashSet<E> newSet = (ConcurrentHashSet<E>) super.clone();
            newSet.map = new ConcurrentHashMap<>(1024);
            newSet.map.putAll(map);
            return newSet;
        } catch (CloneNotSupportedException e) {
            throw new InternalError(e);
        }
    }
    public Spliterator<E> spliterator() {
        return map.keySet().spliterator();
    }
}
