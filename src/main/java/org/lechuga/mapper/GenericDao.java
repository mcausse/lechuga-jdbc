package org.lechuga.mapper;

import java.lang.reflect.ParameterizedType;
import java.util.List;

import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.criteria.Criterion;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.exception.EmptyResultException;
import org.lechuga.jdbc.exception.UnexpectedResultException;

public class GenericDao<E, ID> {

    protected final Class<E> persistentClass;
    protected final EntityManager<E, ID> em;

    @SuppressWarnings("unchecked")
    public GenericDao(IEntityManagerFactory emf) {
        super();
        this.persistentClass = (Class<E>) ((ParameterizedType) getClass().getGenericSuperclass())
                .getActualTypeArguments()[0];
        this.em = emf.getEntityManager(persistentClass);
    }

    public IEntityManagerFactory getEntityManagerFactory() {
        return em.getEntityManagerFactory();
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

    public List<E> loadBy(Criterion c) {
        return em.loadBy(c);
    }

    public E loadUniqueBy(Criterion c) {
        return em.loadUniqueBy(c);
    }

    public List<E> loadBy(Criterion c, List<Order<E>> orders) {
        return em.loadBy(c, orders);
    }

    public E loadUniqueBy(Criterion c, List<Order<E>> orders) {
        return em.loadUniqueBy(c, orders);
    }

    public E loadById(ID idValue) throws EmptyResultException {
        return em.loadById(idValue);
    }

    public <T> List<E> loadByProp(MetaField<E, T> metaField, T value, List<Order<E>> orders) {
        return em.loadByProp(metaField, value, orders);
    }

    public <T> List<E> loadByProp(MetaField<E, T> metaField, T value) {
        return em.loadByProp(metaField, value);
    }

    public <T> E loadUniqueByProp(MetaField<E, T> metaField, T value) throws UnexpectedResultException {
        return em.loadUniqueByProp(metaField, value);
    }

    public List<E> loadAll(List<Order<E>> orders) {
        return em.loadAll(orders);
    }

    public List<E> loadAll() {
        return em.loadAll();
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

    public void update(E entity, List<MetaField<E, ?>> metaFields) {
        em.update(entity, metaFields);
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
