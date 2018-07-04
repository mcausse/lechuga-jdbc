package org.lechuga.annotated;

import java.util.List;

import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.util.Pair;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.TableModel;

public interface IEntityManagerFactory {

    DataAccesFacade getFacade();

    <E, ID> EntityManager<E, ID> getEntityManager(Class<E> entityClass);

    <E> Restrictions<E> getRestrictions(Class<E> entityClass);

    <E> Restrictions<E> getRestrictions(Class<E> entityClass, String alias);

    <E> TableModel<E> getModelByEntityClass(Class<E> entityClass);

    <E> TableModel<E> getModelByMetaClass(Class<?> metaClass);

    CriteriaBuilder createCriteria();

    <E, I, R> List<R> loadManyToMany(OneToMany<E, I> oneToMany, ManyToOne<I, R> manyToOne, E entity);

    <E, I, R> List<Pair<E, List<R>>> loadManyToMany(OneToMany<E, I> oneToMany, ManyToOne<I, R> manyToOne,
            List<E> entities);

}