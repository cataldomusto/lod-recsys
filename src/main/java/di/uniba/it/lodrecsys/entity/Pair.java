package di.uniba.it.lodrecsys.entity;

/**
 * A generic class that represents a simple pair.
 * Unfortunately Java doesn't provide a generic Pair class :)
 */
public class Pair<K, V> {
    public K key;
    public V value;

    public Pair(K key, V value) {
        this.key = key;
        this.value = value;
    }
}
