package typesafecriteria;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.annotated.convention.Conventions;
import org.lechuga.annotated.convention.DefaultConventions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.mapper.Accessor;
import org.lechuga.mapper.autogen.Generator;
import org.lechuga.mapper.handler.EnumeratedHandler;
import org.lechuga.mapper.handler.Handler;
import org.lechuga.mapper.handler.Handlers;
import org.lechuga.mapper.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import typesafecriteria.AAAAAAAAAAAAAA.Entity;
import typesafecriteria.AAAAAAAAAAAAAA.MetaField;

public class EntityManagerFactory2 {// implements IEntityManagerFactory {

    static final Logger LOG = LoggerFactory.getLogger(EntityManagerFactory2.class);

    final DataAccesFacade facade;
    final Conventions conventions;

    final Map<Class<?>, TableModel<?>> metaModels = new LinkedHashMap<>();
    final Map<Class<?>, TableModel<?>> entityModels = new LinkedHashMap<>();

    public EntityManagerFactory2(DataAccesFacade facade, Class<?>... metaEntityClasses) {
        super();
        this.facade = facade;
        this.conventions = new DefaultConventions();

        for (Class<?> metaEntityClass : metaEntityClasses) {
            TableModel<?> model = buildEntityModel(metaEntityClass);
            metaModels.put(metaEntityClass, model);
            entityModels.put(model.getEntityClass(), model);
        }
    }

    public DataAccesFacade getFacade() {
        return facade;
    }

    public <E, ID> EntityManager<E, ID> buildEntityManager(Class<E> entityClass) {
        TableModel<E> model = getModelByEntityClass(entityClass);
        return new EntityManager<>(facade, null/* FIXME this */, model);
    }

    // @Override
    // public <E> QueryBuilder<E> createQuery(Class<E> entityClass) {
    // return new QueryBuilder<>(facade, this, getModel(entityClass), null);
    // }
    //
    // @Override
    // public <E> QueryBuilder<E> createQuery(Class<E> entityClass, String
    // tableAlias) {
    // return new QueryBuilder<>(facade, this, getModel(entityClass), tableAlias);
    // }
    //

    public <E> Restrictions<E> getRestrictions(Class<E> entityClass) {
        return new Restrictions<>(getModelByEntityClass(entityClass));
    }

    public <E> Restrictions<E> getRestrictions(Class<E> entityClass, String alias) {
        return new Restrictions<>(getModelByEntityClass(entityClass), alias);
    }

    // @Override
    public CriteriaBuilder createCriteria() {
        return new CriteriaBuilder(facade, this);
    }

    @SuppressWarnings("unchecked")
    public <E> TableModel<E> getModelByEntityClass(Class<?> entityClass) {
        if (!entityModels.containsKey(entityClass)) {
            throw new RuntimeException("entity not registered: " + entityClass.getName());
        }
        TableModel<E> model = (TableModel<E>) entityModels.get(entityClass);
        return model;
    }

    // @SuppressWarnings("unchecked")
    // public <E> TableModel<E> getModelByMetaClass(Class<?> metaClass) {
    // if (!metaModels.containsKey(metaClass)) {
    // throw new RuntimeException("entity not registered: " + metaClass.getName());
    // }
    // TableModel<E> model = (TableModel<E>) metaModels.get(metaClass);
    // return model;
    // }

    @SuppressWarnings("unchecked")
    protected <E> TableModel<E> buildEntityModel(Class<E> metaEntityClass) {

        LOG.info(metaEntityClass.getName());
        Class<E> entityClass;
        String tableName;
        {
            Entity annoEntity = metaEntityClass.getAnnotation(Entity.class);
            if (annoEntity == null) {
                throw new RuntimeException("expectad a meta-model Class (@" + Entity.class.getName()
                        + "-annotated), but received: " + metaEntityClass);
            }
            entityClass = (Class<E>) annoEntity.entity();
            tableName = annoEntity.table();
        }

        List<Column> cs = new ArrayList<>();
        for (Field f : metaEntityClass.getFields()) {
            if (MetaField.class.isAssignableFrom(f.getType())) {
                MetaField<?, ?> col;
                try {
                    col = (MetaField<?, ?>) f.get(null);
                } catch (Exception e) {
                    throw new RuntimeException(e);
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
                columnName = conventions.columnNameOf(metaFieldField.getName());
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

        return new Column(isPk, columnName, entityClass, metaField, accessor, handler, generator);
    }

}
