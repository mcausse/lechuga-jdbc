package org.lechuga.annotated;

public interface LoadableReference<E, R> {

    R load(IEntityManagerFactory emf, E entity);

}
