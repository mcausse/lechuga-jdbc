package org.lechuga.mapper;

import org.lechuga.annotated.MetaField;

public class Order<E> {

    protected final MetaField<E, ?> metaField;
    protected final String order;

    private Order(MetaField<E, ?> metaField, String order) {
        super();
        this.metaField = metaField;
        this.order = order;
    }

    public static <E> Order<E> asc(MetaField<E, ?> metaField) {
        return new Order<>(metaField, " asc");
    }

    public static <E> Order<E> desc(MetaField<E, ?> metaField) {
        return new Order<>(metaField, " desc");
    }

    public MetaField<?, ?> getMetaField() {
        return metaField;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return metaField + order;
    }

}
