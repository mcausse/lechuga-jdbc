package org.lechuga.mapper;

import org.lechuga.annotated.MetaField;

public class Order {

    final MetaField<?, ?> metaField;
    final String order;

    private Order(MetaField<?, ?> metaField, String order) {
        super();
        this.metaField = metaField;
        this.order = order;
    }

    public static Order asc(MetaField<?, ?> metaField) {
        return new Order(metaField, " asc");
    }

    public static Order desc(MetaField<?, ?> metaField) {
        return new Order(metaField, " desc");
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
