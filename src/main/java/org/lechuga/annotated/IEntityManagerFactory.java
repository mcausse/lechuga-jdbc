package org.lechuga.annotated;

import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.annotated.query.QueryBuilder;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.TableModel;

public interface IEntityManagerFactory {

    DataAccesFacade getFacade();

    <E, ID> EntityManager<E, ID> buildEntityManager(Class<E> entityClass);

    <E> QueryBuilder<E> createQuery(Class<E> entityClass);

    <E> QueryBuilder<E> createQuery(Class<E> entityClass, String tableAlias);

    Restrictions getRestrictions(Class<?> entityClass);

    Restrictions getRestrictions(Class<?> entityClass, String alias);

    CriteriaBuilder createCriteria();

    <E> TableModel<E> getModel(Class<?> entityClass);

}