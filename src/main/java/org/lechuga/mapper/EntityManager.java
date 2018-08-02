package org.lechuga.mapper;

import java.util.Collection;
import java.util.List;

import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Criterion;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.ScalarMappers;
import org.lechuga.jdbc.exception.EmptyResultException;
import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.jdbc.exception.TooManyResultsException;
import org.lechuga.jdbc.exception.UnexpectedResultException;
import org.lechuga.jdbc.queryobject.QueryObject;

public class EntityManager<E, ID> {

    protected final DataAccesFacade facade;
    protected final IEntityManagerFactory emf;
    protected final TableModel<E> model;

    public EntityManager(DataAccesFacade facade, IEntityManagerFactory emf, TableModel<E> model) {
        super();
        this.facade = facade;
        this.emf = emf;
        this.model = model;
    }

    public Class<E> getEntityClass() {
        return model.getEntityClass();
    }

    public TableModel<E> getModel() {
        return model;
    }

    public IEntityManagerFactory getEntityManagerFactory() {
        return emf;
    }

    public DataAccesFacade getDataAccesFacade() {
        return facade;
    }

    // ==============================================
    //
    // CRITERIA BEGIN
    //
    // ==============================================

    public List<E> loadBy(Criterion c) {
        return loadBy(c, null);
    }

    public E loadUniqueBy(Criterion c) {
        return loadUniqueBy(c, null);
    }

    public List<E> loadBy(Criterion c, Sort<E> sort) {
        CriteriaBuilder criteria = createCriteriaTemplate(c, sort);
        return criteria.getExecutor(model.getEntityClass()).load();
    }

    public E loadUniqueBy(Criterion c, Sort<E> sort) {
        CriteriaBuilder criteria = createCriteriaTemplate(c, sort);
        return criteria.getExecutor(model.getEntityClass()).loadUnique();
    }

    protected CriteriaBuilder createCriteriaTemplate(Criterion c, Sort<E> sort) {
        CriteriaBuilder criteria = emf.createCriteria();
        Restrictions<E> r = emf.getRestrictions(model.getEntityClass());
        criteria.append("select {} ", r.all());
        criteria.append("from {} ", r.table());
        criteria.append("where {} ", c);
        if (sort != null && !sort.getOrders().isEmpty()) {
            criteria.append("order by {} ", r.orderBy(sort.getOrders()));
        }
        return criteria;
    }

    // ==============================================
    //
    // CRITERIA END
    //
    // ==============================================

    // // TODO falten mètodes similars, i els corresponents testos
    // @SuppressWarnings("unchecked")
    // public List<E> loadByExample(E example, Sort<E> sort) {
    //
    // List<Criterion> cs = new ArrayList<>();
    // Restrictions<E> r = emf.getRestrictions(model.getEntityClass());
    // for (Column column : this.model.getAllColumns()) {
    // if (column.getPropertyValue(example) != null) {
    // cs.add(r.eq((MetaField<E, Object>) column.getMetafield(), (Object)
    // column.getPropertyValue(example)));
    // }
    // }
    // Criterion c = Restrictions.and(cs); // TODO o or?
    //
    // CriteriaBuilder criteria = createCriteriaTemplate(c, sort);
    // return criteria.getExecutor(model.getEntityClass()).load();
    // }

    public E loadById(ID idValue) throws EmptyResultException {
        QueryObject q = model.queryForLoadById(idValue);
        try {
            return facade.loadUnique(q, model.getRowMapper());
        } catch (TooManyResultsException e) {
            throw new LechugaException("unique result expected, but obtained many: " + q.toString(), e);
        }
    }

    public <T> List<E> loadByProp(MetaField<E, T> metaField, T value, Sort<E> sort) {
        QueryObject q = model.queryForLoadByProp(metaField.getPropertyName(), value, sort);
        return facade.load(q, model.getRowMapper());
    }

    public <T> List<E> loadByProp(MetaField<E, T> metaField, T value) {
        return loadByProp(metaField, value, null);
    }

    public <T> E loadUniqueByProp(MetaField<E, T> metaField, T value) throws UnexpectedResultException {
        QueryObject q = model.queryForLoadByProp(metaField.getPropertyName(), value, null);
        return facade.loadUnique(q, model.getRowMapper());
    }

    public List<E> loadAll(Sort<E> sort) {
        QueryObject q = model.queryForLoadAll(sort);
        return facade.load(q, model.getRowMapper());
    }

    public List<E> loadAll() {
        return loadAll(null);
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

    public void update(E entity, List<MetaField<E, ?>> metaFields) {
        QueryObject q = model.queryForUpdate(entity, metaFields);
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

}
