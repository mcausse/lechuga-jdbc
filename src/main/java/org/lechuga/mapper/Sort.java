package org.lechuga.mapper;

import java.util.ArrayList;
import java.util.List;

import org.lechuga.annotated.MetaField;

public class Sort<E> {

    final List<Order<E>> orders = new ArrayList<>();

    public static <E> Sort<E> singleAsc(MetaField<E, ?> metaField) {
        Sort<E> r = new Sort<>();
        r.asc(metaField);
        return r;
    }

    public static <E> Sort<E> singleDesc(MetaField<E, ?> metaField) {
        Sort<E> r = new Sort<>();
        r.desc(metaField);
        return r;
    }

    public Sort<E> asc(MetaField<E, ?> metaField) {
        this.orders.add(Order.asc(metaField));
        return this;
    }

    public Sort<E> desc(MetaField<E, ?> metaField) {
        this.orders.add(Order.desc(metaField));
        return this;
    }

    public List<Order<E>> getOrders() {
        return orders;
    }

    @Override
    public String toString() {
        return "Sort [orders=" + orders + "]";
    }

}