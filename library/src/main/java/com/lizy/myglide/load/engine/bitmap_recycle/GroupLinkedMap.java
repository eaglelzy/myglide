package com.lizy.myglide.load.engine.bitmap_recycle;

import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by lizy on 16-4-21.
 */
public class GroupLinkedMap<K extends Poolable, V> {

    private final LinkedEntry<K, V> head = new LinkedEntry<>();
    private final Map<K, LinkedEntry<K, V>> keyToEntry = new HashMap<>();

    public void put(K key, V value) {
        LinkedEntry<K, V> entry = keyToEntry.get(key);
        if (entry == null) {
            entry = new LinkedEntry<>(key);
            makeTail(entry);
            keyToEntry.put(key, entry);
        } else {
            key.offer();
        }
        entry.add(value);
    }

    @Nullable
    public V get(K key) {
        LinkedEntry<K, V> entry = keyToEntry.get(key);
        if (entry == null) {
            entry = new LinkedEntry<>(key);
            keyToEntry.put(key, entry);
        } else {
            key.offer();
        }

        makeHead(entry);

        return entry.removeLast();
    }

    @Nullable
    public V removeLast() {
        LinkedEntry<K, V> last = head.prev;

        while (!last.equals(head)) {
            V result = last.removeLast();
            if (result != null) {
                return result;
            } else {
                removeEntry(last);
                keyToEntry.remove(last.key);
                last.key.offer();
            }
            last = last.prev;
        }

        return null;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("GroupedLinkedMap( ");
        LinkedEntry<K, V> current = head.next;
        boolean hadAtLeastOneItem = false;
        while (!current.equals(head)) {
            hadAtLeastOneItem = true;
            sb.append('{').append(current.key).append(':').append(current.size()).append("}, ");
            current = current.next;
        }
        if (hadAtLeastOneItem) {
            sb.delete(sb.length() - 2, sb.length());
        }
        return sb.append(" )").toString();
    }

    private void makeHead(LinkedEntry<K, V> entry) {
        removeEntry(entry);
        entry.prev = head;
        entry.next = head.next;
        updateEntry(entry);
    }

    private void makeTail(LinkedEntry<K, V> entry) {
        removeEntry(entry);
        entry.prev = head.prev;
        entry.next = head;
        updateEntry(entry);
    }

    private void removeEntry(LinkedEntry<K, V> entry) {
        entry.prev.next = entry.next;
        entry.next.prev = entry.prev;
    }

    private void updateEntry(LinkedEntry<K, V> entry) {
        entry.prev.next = entry;
        entry.next.prev = entry;
    }

    private static class LinkedEntry<K, V> {
        private K key;
        private List<V> values;
        LinkedEntry<K, V> prev;
        LinkedEntry<K, V> next;

        LinkedEntry() {
            this(null);
        }

        public LinkedEntry(K key) {
            next = prev = this;
            this.key = key;
        }

        @Nullable
        public V removeLast() {
            int size = size();
            return size > 0 ? values.remove(size - 1) : null;
        }

        public int size() {
            return values != null ? values.size() : 0;
        }

        public void add(V value) {
            if (values == null) {
                values = new ArrayList<>();
            }
            values.add(value);
        }
    }
}
