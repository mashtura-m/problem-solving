package cache;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCache extends LinkedHashMap<Integer, Integer> {
    private final int capacity;

    public LRUCache(int capacity) {
        // initialCapacity, loadFactor, accessOrder (true for LRU)
        super(capacity, 0.75f, true);
        this.capacity = capacity;
    }

    public int get(int key) {
        // Returns the value if present, otherwise returns -1
        return super.getOrDefault(key, -1);
    }

    public void put(int key, int value) {
        super.put(key, value);
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<Integer, Integer> eldest) {
        // If the map grows beyond capacity, the least recently used is evicted
        return size() > capacity;
    }
}