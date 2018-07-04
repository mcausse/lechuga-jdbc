package org.lechuga.annotated;

public class PropPair<E, R, T> {

    public final MetaField<E, T> left;
    public final MetaField<R, T> right;

    public PropPair(MetaField<E, T> left, MetaField<R, T> right) {
        super();
        this.left = left;
        this.right = right;
    }

    @Override
    public String toString() {
        return "PropPair [left=" + left + ", right=" + right + "]";
    }

}