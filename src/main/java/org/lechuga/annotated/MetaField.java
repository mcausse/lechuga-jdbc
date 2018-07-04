package org.lechuga.annotated;

public class MetaField<E, T> {

    protected final String propertyName;

    public MetaField(String propertyName) {
        super();
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    @SuppressWarnings("unchecked")
    public <T2> MetaField<E, T2> castTo(Class<T2> type) {
        MetaField<E, T2> r = (MetaField<E, T2>) this;
        return r;
    }

    @Override
    public String toString() {
        return "MetaField [propertyName=" + propertyName + "]";
    }

}