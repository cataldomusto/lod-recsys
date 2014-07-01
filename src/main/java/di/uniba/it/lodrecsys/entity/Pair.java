package di.uniba.it.lodrecsys.entity;

/**
 * Created by asuglia on 7/1/14.
 */
public class Pair<K, V> {
    public K key;
    public V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
