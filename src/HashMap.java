
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HashMap<K, V> {

    private static final int DEFAULT_CAPACITY = 16;

    private final List<Node<K, V>> table;
    private int size;

    private static class Node<K, V> {
        K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    public HashMap() {
        table = new ArrayList<>(
                Collections.nCopies(DEFAULT_CAPACITY, null)
        );
        size = 0;
    }

    private int getIndex(Object key) {
        return (key.hashCode() & 0x7fffffff) % table.size();
    }

    public void put(K key, V value) {
        if (key == null) {
            throw new IllegalArgumentException("Null keys are not supported");
        }

        int index = getIndex(key);
        Node<K, V> current = table.get(index);

        while (current != null) {
            if (current.key.equals(key)) {
                current.value = value;
                return;
            }

            current = current.next;
        }

        table.set(index, new Node<>(key, value, table.get(index)));
        size++;
    }

    public V get(K key) {
        if (key == null) {
            return null;
        }

        int index = getIndex(key);
        Node<K, V> current = table.get(index);

        while (current != null) {
            if (current.key.equals(key)) {
                return current.value;
            }

            current = current.next;
        }

        return null;
    }

    public V remove(K key) {
        if (key == null) {
            return null;
        }

        int index = getIndex(key);
        Node<K, V> current = table.get(index);
        Node<K, V> previous = null;

        while (current != null) {
            if (current.key.equals(key)) {
                if (previous == null) {
                    table.set(index, current.next);
                } else {
                    previous.next = current.next;
                }

                size--;
                return current.value;
            }

            previous = current;
            current = current.next;
        }

        return null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }
}
