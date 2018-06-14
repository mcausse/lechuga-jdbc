package org.lechuga.mapper;

import java.util.Collection;
import java.util.List;

import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.ScalarMappers;
import org.lechuga.jdbc.exception.EmptyResultException;
import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.jdbc.exception.TooManyResultsException;
import org.lechuga.jdbc.exception.UnexpectedResultException;
import org.lechuga.jdbc.queryobject.QueryObject;
import org.lechuga.mapper.criteria.CriteriaBuilder;
import org.lechuga.mapper.criteria.Criterion;
import org.lechuga.mapper.criteria.Restrictions;
import org.lechuga.mapper.query.QueryBuilder;

public class EntityManager<E, ID> {

    final DataAccesFacade facade;
    final TableModel<E> model;

    public EntityManager(DataAccesFacade facade, TableModel<E> model) {
        super();
        this.facade = facade;
        this.model = model;
    }

    public Class<E> getEntityClass() {
        return model.getEntityClass();
    }

    public TableModel<E> getModel() {
        return model;
    }

    public DataAccesFacade getDataAccesFacade() {
        return facade;
    }

    public QueryBuilder<E> createQuery(String tableAlias) {
        return new QueryBuilder<>(facade, model, tableAlias);
    }

    // ==============================================
    //
    // CRITERIA BEGIN
    //
    // ==============================================

    public CriteriaBuilder createCriteria() {
        return new CriteriaBuilder(facade);
    }

    public Restrictions getRestrictions() {
        return model.getRestrictions();
    }

    public Restrictions getRestrictions(String alias) {
        return new Restrictions(model, alias);
    }

    public List<E> loadBy(Criterion c, Order... orders) {
        CriteriaBuilder criteria = createCriteriaTemplate(c, orders);
        return criteria.getExecutor(this).load();
    }

    public E loadUniqueBy(Criterion c, Order... orders) {
        CriteriaBuilder criteria = createCriteriaTemplate(c, orders);
        return criteria.getExecutor(this).loadUnique();
    }

    protected CriteriaBuilder createCriteriaTemplate(Criterion c, Order... orders) {
        CriteriaBuilder criteria = createCriteria();
        Restrictions r = getRestrictions();
        criteria.append("select {} ", r.all());
        criteria.append("from {} ", r.table());
        criteria.append("where {} ", c);
        if (orders.length > 0) {
            criteria.append("order by {} ", r.orderBy(orders));
        }
        return criteria;
    }

    // ==============================================
    //
    // CRITERIA END
    //
    // ==============================================

    // public List<E> loadBy(Criterion criterion) throws EmptyResultException {
    // }

    public E loadById(ID idValue) throws EmptyResultException {
        QueryObject q = model.queryForLoadById(idValue);
        try {
            return facade.loadUnique(q, model.getRowMapper());
        } catch (TooManyResultsException e) {
            throw new LechugaException("unique result expected, but obtained many: " + q.toString(), e);
        }
    }

    // public void refresh(E entity) throws EmptyResultException {
    // QueryObject q = model.queryForRefresh(entity);
    // try {
    // facade.loadUnique(q, new RowMapper<E>() {
    // @Override
    // public E mapRow(ResultSet rs) throws SQLException {
    // for (PropertyModel p : model.getRegularProps()) {
    // p.loadValue(entity, rs);
    // }
    // return entity;
    // }
    // });
    // } catch (TooManyResultsException e) {
    // throw new BaseException("expected a unique result, but obtained many: " +
    // q.toString(), e);
    // }
    // }

    public List<E> loadByProp(String propertyName, Object value, Order... orders) {
        QueryObject q = model.queryForLoadByProp(propertyName, value, orders);
        return facade.load(q, model.getRowMapper());
    }

    public E loadUniqueByProp(String propertyName, Object value) throws UnexpectedResultException {
        QueryObject q = model.queryForLoadByProp(propertyName, value, new Order[] {});
        return facade.loadUnique(q, model.getRowMapper());
    }

    public List<E> loadAll(Order... orders) {
        QueryObject q = model.queryForLoadAll(orders);
        return facade.load(q, model.getRowMapper());
    }

    public void deleteById(ID idValue) {
        QueryObject q = model.queryForDeleteById(idValue);
        int affectedResults = facade.update(q);
        if (affectedResults != 1) {
            throw new LechugaException(
                    "DELETE: " + q.toString() + ": expected affectedRows=1, but affected: " + affectedResults);
        }
    }

    public void delete(E entity) {
        QueryObject q = model.queryForDelete(entity);
        int affectedResults = facade.update(q);
        if (affectedResults != 1) {
            throw new LechugaException(
                    "DELETE: " + q.toString() + ": expected affectedRows=1, but affected: " + affectedResults);
        }
    }

    /**
     * fa un {@link #insert(Object)} o un {@link #update(Object)}, segons convingui.
     *
     * <pre>
     * si almenys una PK és Autogen:
     *         insert: totes les PK han d'estar informades, menys les Autogen.
     *         update: totes les PK han d'estar informades.
     * sino
     *         si almenys una PK està a null =&gt; error
     *
     *         si exist()
     *             update()
     *         sino
     *             insert()
     *         fisi
     * fisi
     * </pre>
     */
    public void store(final E entity) {

        Collection<Column> autogens = model.getAutogeneratedProperties();
        boolean algunaIdAutogen = !autogens.isEmpty();

        for (Column p : autogens) {
            /**
             * si una propietat @Id és primitiva, el seu valor mai serà null (p.ex. serà 0)
             * i l'store() no funcionarà. Si es té una @Id primitiva, usar insert()/update()
             * en comptes d'store().
             */
            if (p.getPropertyType().isPrimitive()) {
                throw new LechugaException(
                        "@Id-annotated field is of primitive type: use insert()/update() instead of store(): "
                                + entity.getClass().getSimpleName() + "#" + p.getPropertyName());
            }
        }

        if (algunaIdAutogen) {

            boolean insert = false;
            for (Column p : model.getIdColumns()) {
                if (p.getGenerator() == null) {
                    if (p.getValueForJdbc(entity) == null) {
                        throw new LechugaException("una propietat PK no-autogenerada té valor null en store(): "
                                + entity.getClass().getSimpleName() + "#" + p.getPropertyName());
                    }
                } else {
                    if (p.getValueForJdbc(entity) == null) {
                        insert = true;
                    }
                }
            }

            if (insert) {
                insert(entity);
            } else {
                update(entity);
            }

        } else {

            for (Column p : model.getIdColumns()) {
                if (p.getValueForJdbc(entity) == null) {
                    throw new LechugaException("una propietat PK no-autogenerada té valor null en store(): "
                            + entity.getClass().getSimpleName() + "#" + p.getPropertyName());
                }
            }

            boolean update = exists(entity);
            if (update) {
                update(entity);
            } else {
                insert(entity);
            }
        }
    }

    public boolean exists(final E entity) {
        QueryObject q = model.queryExists(entity);
        try {
            long count = facade.loadUnique(q, ScalarMappers.LONG);
            return count > 0L;
        } catch (EmptyResultException e) {
            throw new LechugaException("EXISTS: " + q.toString(), e);
        } catch (TooManyResultsException e) {
            throw new LechugaException("duplicated entity found with the same PK? " + entity + " => " + q, e);
        }
    }

    public boolean existsById(final ID id) {
        QueryObject q = model.queryExistsById(id);
        try {
            long count = facade.loadUnique(q, ScalarMappers.LONG);
            return count > 0L;
        } catch (EmptyResultException e) {
            throw new LechugaException("EXISTS: " + q.toString(), e);
        } catch (TooManyResultsException e) {
            throw new LechugaException("duplicated entity found with the same PK? " + id + " => " + q, e);
        }
    }

    // https://docs.spring.io/spring-data/commons/docs/current/api/org/springframework/data/repository/CrudRepository.html

    public void update(E entity) {
        QueryObject q = model.queryForUpdate(entity);
        int affectedResults = facade.update(q);

        if (affectedResults != 1) {
            throw new LechugaException(
                    "UPDATE: " + q.toString() + ": expected affectedRows=1, but affected: " + affectedResults);
        }
    }

    public void update(E entity, String... properties) {
        QueryObject q = model.queryForUpdate(entity, properties);
        int affectedResults = facade.update(q);

        if (affectedResults != 1) {
            throw new LechugaException(
                    "UPDATE: " + q.toString() + ": expected affectedRows=1, but affected: " + affectedResults);
        }
    }

    public void insert(E entity) {

        model.generateBeforeInsert(facade, entity);

        QueryObject q = model.queryForInsert(entity);
        facade.update(q);

        model.generateAfterInsert(facade, entity);
    }

    public void store(Iterable<E> entities) {
        for (E e : entities) {
            store(e);
        }
    }

    public void insert(Iterable<E> entities) {
        for (E e : entities) {
            insert(e);
        }
    }

    public void update(Iterable<E> entities) {
        for (E e : entities) {
            update(e);
        }
    }

    public void delete(Iterable<E> entities) {
        for (E e : entities) {
            delete(e);
        }
    }

    // public <O> List<O> getManyRefs(E entity, String entityAlias, EntityManager<O,
    // ?> refEm, String refAlias,
    // String where) {
    //
    // QueryBuilder<O> c = refEm.createQuery(refAlias);
    // c.addEm(entityAlias, this);
    // c.append("select {" + refAlias + ".*} from {" + refAlias + "} where ");
    // c.append(where);
    // return c.getExecutor().load();
    // }

    // // // TODO
    // public <E2> List<E2> loadOneToMany(E self, EntityManager<E2, ?> otherEm,
    // String... otherFkPropertyNames) {
    //
    // Restrictions tr=getRestrictions("s");
    // Restrictions or=otherEm.getRestrictions("o");
    //
    // for(String oc : otherFkPropertyNames) {
    // or.
    // }
    //
    //
    // CriteriaBuilder c = otherEm.createCriteria();
    // c.append("select {} from {} ", tr.all(), tr.table());
    // c.append("join {} ",or.table());
    //
    // return null;
    // }
    // //
    // public <E2> E2 loadManyToOne(E self, String selfFkPropertyName,
    // EntityManager<E2, Object> otherEm) {
    // Column thisFk = model.findColumnByName(selfFkPropertyName);
    // Object thisFkValue = thisFk.accessor.get(self);
    // return otherEm.loadById(thisFkValue);
    // }

}
