package org.lechuga;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.exception.EmptyResultException;
import org.lechuga.jdbc.exception.UnexpectedResultException;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.Order;
import org.lechuga.mapper.TableModel;
import org.lechuga.mapper.criteria.CriteriaBuilder;
import org.lechuga.mapper.criteria.Criterion;
import org.lechuga.mapper.criteria.Restrictions;
import org.lechuga.mapper.query.QueryBuilder;

public class GenericDao<T, ID> {

    final Class<T> persistentClass;
    final Class<ID> idClass;
    final EntityManager<T, ID> em;

    @SuppressWarnings("unchecked")
    public GenericDao(DataAccesFacade facade) {
        super();
        this.persistentClass = (Class<T>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.idClass = (Class<ID>) ((ParameterizedType) getClass().getGenericSuperclass()).getActualTypeArguments()[1];
        this.em = new EntityManagerFactory(facade).build(persistentClass, idClass);
    }

    public EntityManager<T, ID> getEntityManager() {
        return em;
    }

    public Class<T> getEntityClass() {
        return em.getEntityClass();
    }

    public TableModel<T> getModel() {
        return em.getModel();
    }

    public DataAccesFacade getDataAccesFacade() {
        return em.getDataAccesFacade();
    }

    public QueryBuilder<T> createQuery(String tableAlias) {
        return em.createQuery(tableAlias);
    }

    public CriteriaBuilder createCriteria() {
        return em.createCriteria();
    }

    public Restrictions getRestrictions() {
        return em.getRestrictions();
    }

    public Restrictions getRestrictions(String alias) {
        return em.getRestrictions(alias);
    }

    public List<T> loadBy(Criterion c, Order... orders) {
        return em.loadBy(c, orders);
    }

    public T loadUniqueBy(Criterion c, Order... orders) {
        return em.loadUniqueBy(c, orders);
    }

    public T loadById(ID idValue) throws EmptyResultException {
        return em.loadById(idValue);
    }

    public List<T> loadByProp(String propertyName, Object value, Order... orders) {
        return em.loadByProp(propertyName, value, orders);
    }

    public T loadUniqueByProp(String propertyName, Object value) throws UnexpectedResultException {
        return em.loadUniqueByProp(propertyName, value);
    }

    public List<T> loadAll(Order... orders) {
        return em.loadAll(orders);
    }

    public void deleteById(ID idValue) {
        em.deleteById(idValue);
    }

    public void delete(T entity) {
        em.delete(entity);
    }

    public void store(T entity) {
        em.store(entity);
    }

    public boolean exists(T entity) {
        return em.exists(entity);
    }

    public boolean existsById(ID id) {
        return em.existsById(id);
    }

    public void update(T entity) {
        em.update(entity);
    }

    public void update(T entity, String... properties) {
        em.update(entity, properties);
    }

    public void insert(T entity) {
        em.insert(entity);
    }

    public void store(Iterable<T> entities) {
        em.store(entities);
    }

    public void insert(Iterable<T> entities) {
        em.insert(entities);
    }

    public void update(Iterable<T> entities) {
        em.update(entities);
    }

    public void delete(Iterable<T> entities) {
        em.delete(entities);
    }

}
