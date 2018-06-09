package org.lechuga.mapper.ents;

public class Apoyo {

    String key;
    String value;

    public Apoyo() {
        super();
    }

    public Apoyo(String key, String value) {
        super();
        this.key = key;
        this.value = value;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    @Override
    public String toString() {
        return "Apoyo [key=" + key + ", value=" + value + "]";
    }

}
