package org.lechuga.annotated;

public class PropPair<E, R> {

    public final MetaField<E, ?> left;
    public final MetaField<R, ?> right;

    public PropPair(MetaField<E, ?> left, MetaField<R, ?> right) {
        super();
        this.left = left;
        this.right = right;
    }

}