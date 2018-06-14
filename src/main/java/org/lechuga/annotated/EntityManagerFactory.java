package org.lechuga.annotated;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.annotated.anno.Table;
import org.lechuga.annotated.anno.Transient;
import org.lechuga.annotated.convention.Conventions;
import org.lechuga.annotated.convention.DefaultConventions;
import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.annotated.query.QueryBuilder;
import org.lechuga.annotated.util.AnnoReflectUtils;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.mapper.Accessor;
import org.lechuga.mapper.Column;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.TableModel;
import org.lechuga.mapper.autogen.Generator;
import org.lechuga.mapper.handler.EnumeratedHandler;
import org.lechuga.mapper.handler.Handler;
import org.lechuga.mapper.handler.Handlers;
import org.lechuga.mapper.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerFactory implements IEntityManagerFactory {

    static final Logger LOG = LoggerFactory.getLogger(EntityManagerFactory.class);

    final DataAccesFacade facade;
    final Conventions conventions;
    final Map<Class<?>, TableModel<?>> models = new LinkedHashMap<>();

    public EntityManagerFactory(DataAccesFacade facade, Class<?>... entityClasses) {
        super();
        this.facade = facade;
        this.conventions = new DefaultConventions();

        for (Class<?> ec : entityClasses) {
            models.put(ec, buildEntityModel(ec));
        }
    }

    @Override
    public DataAccesFacade getFacade() {
        return facade;
    }

    @Override
    public <E, ID> EntityManager<E, ID> buildEntityManager(Class<E> entityClass) {
        TableModel<E> model = getModel(entityClass);
        return new EntityManager<>(facade, this, model);
    }

    @Override
    public <E> QueryBuilder<E> createQuery(Class<E> entityClass) {
        return new QueryBuilder<>(facade, this, getModel(entityClass), null);
    }

    @Override
    public <E> QueryBuilder<E> createQuery(Class<E> entityClass, String tableAlias) {
        return new QueryBuilder<>(facade, this, getModel(entityClass), tableAlias);
    }

    @Override
    public Restrictions getRestrictions(Class<?> entityClass) {
        return new Restrictions(getModel(entityClass));
    }

    @Override
    public Restrictions getRestrictions(Class<?> entityClass, String alias) {
        return new Restrictions(getModel(entityClass), alias);
    }

    @Override
    public CriteriaBuilder createCriteria() {
        return new CriteriaBuilder(facade, this);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <E> TableModel<E> getModel(Class<?> entityClass) {
        if (!models.containsKey(entityClass)) {
            throw new RuntimeException("entity not registered: " + entityClass.getName());
        }
        TableModel<E> model = (TableModel<E>) models.get(entityClass);
        return model;
    }

    protected <E> TableModel<E> buildEntityModel(Class<E> entityClass) {

        LOG.info(entityClass.getName());
        String tableName;
        {
            Table annoTable = entityClass.getAnnotation(Table.class);
            if (annoTable == null) {
                tableName = conventions.tableNameOf(entityClass);
            } else {
                tableName = annoTable.value();
            }
        }

        TableModel<E> r = new TableModel<>(entityClass, tableName);
        LOG.info(entityClass.getName() + ": " + r);

        Map<String, Field> fs = AnnoReflectUtils.getFields(entityClass);
        {
            for (Entry<String, Field> f : fs.entrySet()) {

                if (f.getValue().getAnnotation(Transient.class) != null) {
                    continue;
                }

                Column c = buildPropertyModel(entityClass, f.getKey(), f.getValue());
                LOG.info(entityClass.getName() + ": " + c);
                r.addColumn(c);
            }
        }
        return r;
    }

    protected Column buildPropertyModel(Class<?> entityClass, String propertyPathName, Field p) {

        Generator generator;
        {
            Generated annoGenerated = p.getAnnotation(Generated.class);
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
            org.lechuga.annotated.anno.Column annoColumn = p.getAnnotation(org.lechuga.annotated.anno.Column.class);
            if (annoColumn == null) {
                columnName = conventions.columnNameOf(p.getName());
            } else {
                columnName = annoColumn.value();
            }
        }

        boolean isPk = p.getAnnotation(Id.class) != null;

        Handler handler;
        {
            EnumHandler enumHandler = p.getAnnotation(EnumHandler.class);
            if (enumHandler != null) {
                if (!Enum.class.isAssignableFrom(p.getType())) {
                    throw new LechugaException("property '" + entityClass.getSimpleName() + "#" + p.getName()
                            + "' is not of Enum type: " + p.getType().getName());
                }
                handler = new EnumeratedHandler(p.getType());
            } else {
                CustomHandler annoCustomHandler = p.getAnnotation(CustomHandler.class);
                if (annoCustomHandler == null) {
                    handler = Handlers.getHandlerFor(p.getType());
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

        Accessor accessor = new Accessor(entityClass, propertyPathName);
        return new Column(isPk, columnName, accessor, handler, generator);
    }

}
