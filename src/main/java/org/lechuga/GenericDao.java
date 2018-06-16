package org.lechuga;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.criteria.Criterion;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.exception.EmptyResultException;
import org.lechuga.jdbc.exception.UnexpectedResultException;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.MetaField;
import org.lechuga.mapper.Order;
import org.lechuga.mapper.TableModel;

public class GenericDao<E, ID> {

    final protected IEntityManagerFactory emf;
    final Class<E> persistentClass;
    final EntityManager<E, ID> em;

    @SuppressWarnings("unchecked")
    public GenericDao(IEntityManagerFactory emf) {
        super();
        this.emf = emf;
        this.persistentClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.em = emf.buildEntityManager(persistentClass);
    }

    public IEntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public EntityManager<E, ID> getEntityManager() {
        return em;
    }

    public Class<E> getEntityClass() {
        return em.getEntityClass();
    }

    public TableModel<E> getModel() {
        return em.getModel();
    }

    public DataAccesFacade getDataAccesFacade() {
        return em.getDataAccesFacade();
    }

    public List<E> loadBy(Criterion c, Order... orders) {
        return em.loadBy(c, orders);
    }

    public E loadUniqueBy(Criterion c, Order... orders) {
        return em.loadUniqueBy(c, orders);
    }

    public E loadById(ID idValue) throws EmptyResultException {
        return em.loadById(idValue);
    }

    public <T> List<E> loadByProp(MetaField<E, T> metaField, T value, Order... orders) {
        return em.loadByProp(metaField, value, orders);
    }

    public <T> E loadUniqueByProp(MetaField<E, T> metaField, T value) throws UnexpectedResultException {
        return em.loadUniqueByProp(metaField, value);
    }

    public List<E> loadAll(Order... orders) {
        return em.loadAll(orders);
    }

    public void deleteById(ID idValue) {
        em.deleteById(idValue);
    }

    public void delete(E entity) {
        em.delete(entity);
    }

    public void store(E entity) {
        em.store(entity);
    }

    public boolean exists(E entity) {
        return em.exists(entity);
    }

    public boolean existsById(ID id) {
        return em.existsById(id);
    }

    public void update(E entity) {
        em.update(entity);
    }

    public void update(E entity, String... properties) {
        em.update(entity, properties);
    }

    public void insert(E entity) {
        em.insert(entity);
    }

    public void store(Iterable<E> entities) {
        em.store(entities);
    }

    public void insert(Iterable<E> entities) {
        em.insert(entities);
    }

    public void update(Iterable<E> entities) {
        em.update(entities);
    }

    public void delete(Iterable<E> entities) {
        em.delete(entities);
    }

}
