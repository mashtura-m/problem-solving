import cache.LRUCache;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {
    public static void main(String[] args) {
        LRUCache lruCache=new LRUCache(2);
        lruCache.put(1,1);
        lruCache.put(2,2);
        System.out.println("GET: "+lruCache.get(5));
        lruCache.put(3,3);
        System.out.println(lruCache);
        System.out.println("GET: "+lruCache.get(2));
        lruCache.put(4,4);
        System.out.println(lruCache);
        System.out.println("GET: "+lruCache.get(1));
        System.out.println("GET: "+lruCache.get(3));
        System.out.println("GET: "+lruCache.get(4));

    }
}