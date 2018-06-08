package org.frijoles.annotated;

import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.Map;
import java.util.Map.Entry;

import org.frijoles.annotated.anno.CustomHandler;
import org.frijoles.annotated.anno.EnumHandler;
import org.frijoles.annotated.anno.Generated;
import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;
import org.frijoles.annotated.anno.Transient;
import org.frijoles.annotated.convention.Conventions;
import org.frijoles.annotated.convention.DefaultConventions;
import org.frijoles.annotated.util.AnnoReflectUtils;
import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.exception.BaseException;
import org.frijoles.mapper.Accessor;
import org.frijoles.mapper.Column;
import org.frijoles.mapper.EntityManager;
import org.frijoles.mapper.TableModel;
import org.frijoles.mapper.autogen.Generator;
import org.frijoles.mapper.handler.EnumeratedHandler;
import org.frijoles.mapper.handler.Handler;
import org.frijoles.mapper.handler.Handlers;
import org.frijoles.mapper.util.ReflectUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityManagerFactory {

    static final Logger LOG = LoggerFactory.getLogger(EntityManagerFactory.class);

    final DataAccesFacade facade;
    final Conventions conventions;

    public EntityManagerFactory(DataAccesFacade facade, Conventions conventions) {
        super();
        this.facade = facade;
        this.conventions = conventions;
    }

    public EntityManagerFactory(DataAccesFacade facade) {
        this(facade, new DefaultConventions());
    }

    public <E, ID> EntityManager<E, ID> build(Class<E> entityClass, Class<ID> pkClass) {
        LOG.info(entityClass.getName());
        TableModel<E> model = buildEntityModel(entityClass);
        return new EntityManager<>(facade, model);
    }

    protected <E> TableModel<E> buildEntityModel(Class<E> entityClass) {

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
                    throw new BaseException("instancing " + annoGenerated.value().getClass().getSimpleName() + "("
                            + Arrays.toString(annoGenerated.args()) + ")", e);
                }
            }
        }
        String columnName;
        {
            org.frijoles.annotated.anno.Column annoColumn = p.getAnnotation(org.frijoles.annotated.anno.Column.class);
            if (annoColumn == null) {
                columnName = conventions.columnNameOf(p.getName());
            } else {
                columnName = annoColumn.value();
            }
        }

        boolean isPk;
        {
            Id annoId = p.getAnnotation(Id.class);
            isPk = annoId != null;
        }

        Handler handler;
        {
            EnumHandler enumHandler = p.getAnnotation(EnumHandler.class);
            if (enumHandler != null) {
                if (!Enum.class.isAssignableFrom(p.getType())) {
                    throw new BaseException("property '" + entityClass.getSimpleName() + "#" + p.getName()
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
                        throw new BaseException("instancing " + annoCustomHandler.value().getClass().getSimpleName()
                                + "(" + Arrays.toString(annoCustomHandler.args()) + ")", e);
                    }
                }
            }
        }

        Accessor accessor = new Accessor(entityClass, propertyPathName);
        return new Column(isPk, columnName, accessor, handler, generator);
    }

}