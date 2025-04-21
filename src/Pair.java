package src;

public class Pair {
    private final String key;
    private int value;

    public Pair(String key, int value) {
        this.key = key;
        this.value = value;
    }

    public void increment() {
        value++;
    }

    public String getKey() {
        return key;
    }

    public Integer getValue() {
        return value;
    }
}
