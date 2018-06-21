package org.lechuga.annotated;

import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.TableModel;

public interface IEntityManagerFactory {

    DataAccesFacade getFacade();

    // TODO renombrar a getEntityManager
    <E, ID> EntityManager<E, ID> buildEntityManager(Class<E> entityClass);

    <E> Restrictions<E> getRestrictions(Class<E> entityClass);

    <E> Restrictions<E> getRestrictions(Class<E> entityClass, String alias);

    <E> TableModel<E> getModelByEntityClass(Class<?> entityClass);

    <E> TableModel<E> getModelByMetaClass(Class<?> metaClass);

    CriteriaBuilder createCriteria();

}