package org.lechuga.annotated;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.annotated.convention.Conventions;
import org.lechuga.annotated.convention.DefaultConventions;
import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.jdbc.util.Pair;
import org.lechuga.mapper.Accessor;
import org.lechuga.mapper.Column;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.ReflectUtils;
import org.lechuga.mapper.TableModel;
import org.lechuga.mapper.autogen.Generator;
import org.lechuga.mapper.handler.EnumeratedHandler;
import org.lechuga.mapper.handler.Handler;
import org.lechuga.mapper.handler.Handlers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerFactory implements IEntityManagerFactory {// implements IEntityManagerFactory {

    static final Logger LOG = LoggerFactory.getLogger(EntityManagerFactory.class);

    protected final DataAccesFacade facade;
    protected final Conventions conventions;

    protected final Map<Class<?>, TableModel<?>> metaModels = new LinkedHashMap<>();
    protected final Map<Class<?>, TableModel<?>> entityModels = new LinkedHashMap<>();

    public EntityManagerFactory(DataAccesFacade facade, Class<?>... metaEntityClasses) {
        super();
        this.facade = facade;
        this.conventions = new DefaultConventions();

        for (Class<?> metaEntityClass : metaEntityClasses) {
            TableModel<?> model = buildEntityModel(metaEntityClass);
            metaModels.put(metaEntityClass, model);
            entityModels.put(model.getEntityClass(), model);
        }
    }

    @Override
    public DataAccesFacade getFacade() {
        return facade;
    }

    @Override
    public <E, ID> EntityManager<E, ID> getEntityManager(Class<E> entityClass) {
        TableModel<E> model = getModelByEntityClass(entityClass);
        return new EntityManager<>(facade, this, model);
    }

    @Override
    public <E> Restrictions<E> getRestrictions(Class<E> entityClass) {
        return new Restrictions<>(getModelByEntityClass(entityClass));
    }

    @Override
    public <E> Restrictions<E> getRestrictions(Class<E> entityClass, String alias) {
        return new Restrictions<>(getModelByEntityClass(entityClass), alias);
    }

    @Override
    public CriteriaBuilder createCriteria() {
        return new CriteriaBuilder(facade, this);
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> TableModel<E> getModelByEntityClass(Class<E> entityClass) {
        if (!entityModels.containsKey(entityClass)) {
            throw new LechugaException(
                    "entity not registered: " + entityClass.getName() + "; registered= " + entityModels.keySet());
        }
        TableModel<E> model = (TableModel<E>) entityModels.get(entityClass);
        return model;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <E> TableModel<E> getModelByMetaClass(Class<?> metaClass) {
        if (!metaModels.containsKey(metaClass)) {
            throw new LechugaException(
                    "meta-entity not registered: " + metaClass.getName() + "; registered= " + metaModels.keySet());

        }
        TableModel<E> model = (TableModel<E>) metaModels.get(metaClass);
        return model;
    }

    @SuppressWarnings("unchecked")
    protected <E> TableModel<E> buildEntityModel(Class<E> metaEntityClass) {

        try {

            LOG.info(metaEntityClass.getName());
            Class<E> entityClass;
            String tableName;
            {
                Entity annoEntity = metaEntityClass.getAnnotation(Entity.class);
                if (annoEntity == null) {
                    throw new LechugaException("expectad a meta-model Class (@" + Entity.class.getName()
                            + "-annotated), but received: " + metaEntityClass);
                }
                entityClass = (Class<E>) annoEntity.entity();

                if (annoEntity.table().isEmpty()) {
                    tableName = conventions.tableNameOf(entityClass);
                } else {
                    tableName = annoEntity.table();
                }
            }

            List<Column> cs = new ArrayList<>();
            for (Field f : metaEntityClass.getFields()) {
                if (MetaField.class.isAssignableFrom(f.getType())) {
                    MetaField<?, ?> col;
                    try {
                        col = (MetaField<?, ?>) f.get(null);
                    } catch (Exception e) {
                        throw new LechugaException(
                                "reading static field: " + metaEntityClass.getName() + "#" + f.getName(), e);
                    }

                    if (col == null) {
                        continue;
                    }

                    Column c = buildPropertyModel(entityClass, col, f);
                    LOG.info(entityClass.getName() + ": " + c);
                    cs.add(c);
                }
            }

            TableModel<E> r = new TableModel<>(entityClass, metaEntityClass, tableName, cs);
            LOG.info(entityClass.getName() + ": " + r);
            return r;

        } catch (Exception e) {
            throw new LechugaException("building model for: " + metaEntityClass.getName(), e);
        }

    }

    protected Column buildPropertyModel(Class<?> entityClass, MetaField<?, ?> metaField, Field metaFieldField) {

        Generator generator;
        {
            Generated annoGenerated = metaFieldField.getAnnotation(Generated.class);
            if (annoGenerated == null) {
                generator = null;
            } else {
                try {
                    generator = ReflectUtils.newInstance(annoGenerated.value(), annoGenerated.args());
                } catch (Exception e) {
                    throw new LechugaException("instancing " + annoGenerated.value().getClass().getSimpleName() + "("
                            + Arrays.toString(annoGenerated.args()) + ")", e);
                }
            }
        }
        String columnName;
        {
            org.lechuga.annotated.anno.Column annoColumn = metaFieldField
                    .getAnnotation(org.lechuga.annotated.anno.Column.class);
            if (annoColumn == null) {
                String[] proponameParts = metaField.getPropertyName().split("\\.");
                String proponame = proponameParts[proponameParts.length - 1];
                columnName = conventions.columnNameOf(proponame);
            } else {
                columnName = annoColumn.value();
            }
        }

        boolean isPk = metaFieldField.getAnnotation(Id.class) != null;

        Accessor accessor = new Accessor(entityClass, metaField.getPropertyName());

        Handler handler;
        {
            EnumHandler enumHandler = metaFieldField.getAnnotation(EnumHandler.class);
            if (enumHandler != null) {
                if (!Enum.class.isAssignableFrom(accessor.getPropertyFinalType())) {
                    throw new LechugaException(
                            "property '" + entityClass.getSimpleName() + "#" + metaFieldField.getName()
                                    + "' is not of Enum type: " + accessor.getPropertyFinalType().getName());
                }
                handler = new EnumeratedHandler(accessor.getPropertyFinalType());
            } else {
                CustomHandler annoCustomHandler = metaFieldField.getAnnotation(CustomHandler.class);
                if (annoCustomHandler == null) {
                    handler = Handlers.getHandlerFor(accessor.getPropertyFinalType());
                } else {
                    try {
                        handler = ReflectUtils.newInstance(annoCustomHandler.value(), annoCustomHandler.args());
                    } catch (Exception e) {
                        throw new LechugaException("instancing " + annoCustomHandler.value().getClass().getSimpleName()
                                + "(" + Arrays.toString(annoCustomHandler.args()) + ")", e);
                    }
                }
            }
        }

        return new Column(isPk, columnName, metaField, accessor, handler, generator);
    }

    // TODO Order?
    @Override
    public <E, I, R> List<R> loadManyToMany(OneToMany<E, I> oneToMany, ManyToOne<I, R> manyToOne, E entity) {
        List<I> is = oneToMany.load(this, entity);
        List<Pair<I, R>> pairs = manyToOne.load(this, is);
        List<R> r = new ArrayList<>();
        pairs.forEach(p -> r.add(p.getRight()));
        return r;
    }

    // TODO Order?
    @Override
    public <E, I, R> List<Pair<E, List<R>>> loadManyToMany(OneToMany<E, I> oneToMany, ManyToOne<I, R> manyToOne,
            List<E> entities) {
        List<Pair<E, List<R>>> r = new ArrayList<>();
        for (E entity : entities) {
            r.add(new Pair<>(entity, loadManyToMany(oneToMany, manyToOne, entity)));
        }
        return r;
    }

}
