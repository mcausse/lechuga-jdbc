package org.lechuga.annotated;

import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.TableModel;

public interface IEntityManagerFactory {

    DataAccesFacade getFacade();

    <E, ID> EntityManager<E, ID> buildEntityManager(Class<E> entityClass);

    <E> Restrictions<E> getRestrictions(Class<E> entityClass);

    <E> Restrictions<E> getRestrictions(Class<E> entityClass, String alias);

    // @Override
    CriteriaBuilder createCriteria();

    <E> TableModel<E> getModelByEntityClass(Class<?> entityClass);

}