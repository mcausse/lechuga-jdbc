package org.moncheta.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.StringJoiner;

import org.moncheta.jdbc.DataAccesFacade;
import org.moncheta.jdbc.RowMapper;
import org.moncheta.jdbc.queryobject.QueryObject;
import org.moncheta.jdbc.queryobject.SimpleQuery;
import org.moncheta.mapper.autogen.Generator;
import org.moncheta.mapper.handler.Handler;
import org.moncheta.mapper.util.ReflectUtils;

public class Moncheta2019 {

    public static class TableModel<E> {

        final Class<E> entityClass;
        final String tableName;

        final Set<Column> idColumns = new LinkedHashSet<>();
        final Set<Column> regularColumns = new LinkedHashSet<>();
        final Set<Column> allColumns = new LinkedHashSet<>();
        final Map<String, Column> propsMap = new LinkedHashMap<>();

        final RowMapper<E> rowMapper;

        public TableModel(Class<E> entityClass, String tableName) {
            super();
            this.entityClass = entityClass;
            this.tableName = tableName;

            this.rowMapper = new RowMapper<E>() {
                @Override
                public E mapRow(ResultSet rs) throws SQLException {
                    E entity = ReflectUtils.newInstance(entityClass);
                    for (Column c : allColumns) {
                        c.loadValue(entity, rs);
                    }
                    return entity;
                }
            };
        }

        public void addColumn(Column c) {
            if (c.isId()) {
                idColumns.add(c);
            } else {
                regularColumns.add(c);
            }
            allColumns.add(c);
            propsMap.put(c.getColumnName(), c);
        }

        public Class<E> getEntityClass() {
            return entityClass;
        }

        public String getTableName() {
            return tableName;
        }

        public RowMapper<E> getRowMapper() {
            return rowMapper;
        }

        public Column findColumnByName(String name) {
            if (!this.propsMap.containsKey(name)) {
                throw new RuntimeException("property not found: " + name + "; in entity " + entityClass);
            }
            return this.propsMap.get(name);
        }

        protected String orderBy(Order[] orders) {
            StringJoiner r = new StringJoiner(",");
            for (Order o : orders) {
                Column c = findColumnByName(o.getProperty());
                r.add(c.getColumnName() + o.getOrder());
            }
            return " order by " + r.toString();
        }

        public QueryObject queryForLoadById(Object id) {
            SimpleQuery q = new SimpleQuery();
            q.append("select ");
            {
                StringJoiner j = new StringJoiner(",");
                for (Column c : allColumns) {
                    j.add(c.getColumnName());
                }
                q.append(j.toString());
            }
            q.append(" from ");
            q.append(tableName);
            q.append(" where ");
            {
                StringJoiner j = new StringJoiner(" and ");
                for (Column c : idColumns) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.getValueForJdbc(id, 1));
                }
                q.append(j.toString());
            }
            return q;
        }

        public QueryObject queryForLoadAll(Order[] orders) {
            SimpleQuery q = new SimpleQuery();
            q.append("select ");
            {
                StringJoiner j = new StringJoiner(",");
                for (Column c : allColumns) {
                    j.add(c.getColumnName());
                }
                q.append(j.toString());
            }
            q.append(" from ");
            q.append(tableName);
            if (orders.length >= 0) {
                q.append(orderBy(orders));
            }
            return q;
        }

        public QueryObject queryForInsert(E entity) {
            SimpleQuery q = new SimpleQuery();
            q.append("insert into ");
            q.append(tableName);
            q.append(" (");
            {
                StringJoiner j = new StringJoiner(",");
                for (Column c : allColumns) {
                    j.add(c.getColumnName());
                }
                q.append(j.toString());
            }
            q.append(") values (");
            {
                StringJoiner j = new StringJoiner(",");
                for (Column c : allColumns) {
                    j.add("?");
                    q.addArg(c.getValueForJdbc(entity));
                }
                q.append(j.toString());
            }
            q.append(")");
            return q;
        }

        public QueryObject queryForUpdate(E entity) {
            SimpleQuery q = new SimpleQuery();
            q.append("update ");
            q.append(tableName);
            q.append(" set ");
            {
                StringJoiner j = new StringJoiner(",");
                for (Column c : regularColumns) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.getValueForJdbc(entity));
                }
                q.append(j.toString());
            }
            q.append(" where ");
            {
                StringJoiner j = new StringJoiner(" and ");
                for (Column c : idColumns) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.getValueForJdbc(entity));
                }
                q.append(j.toString());
            }

            return q;
        }

        public QueryObject queryForDeleteById(Object id) {
            SimpleQuery q = new SimpleQuery();
            q.append("delete from ");
            q.append(tableName);
            q.append(" where ");
            {
                StringJoiner j = new StringJoiner(" and ");
                for (Column c : idColumns) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.getValueForJdbc(id, 1));
                }
                q.append(j.toString());
            }
            return q;
        }

        public QueryObject queryForDelete(E entity) {
            SimpleQuery q = new SimpleQuery();
            q.append("delete from ");
            q.append(tableName);
            q.append(" where ");
            {
                StringJoiner j = new StringJoiner(" and ");
                for (Column c : idColumns) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.getValueForJdbc(entity));
                }
                q.append(j.toString());
            }
            return q;
        }

        public QueryObject queryForLoadByProp(String propertyName, Object value, Order[] orders) {
            SimpleQuery q = new SimpleQuery();
            q.append("select ");
            {
                StringJoiner j = new StringJoiner(",");
                for (Column c : allColumns) {
                    j.add(c.getColumnName());
                }
                q.append(j.toString());
            }
            q.append(" from ");
            q.append(tableName);
            q.append(" where ");

            Column c = findColumnByName(propertyName);
            q.append(c.getColumnName());
            q.append("=?");

            q.addArg(c.handler.getJdbcValue(value)); // TODO

            if (orders.length >= 0) {
                q.append(orderBy(orders));
            }
            return q;
        }

        public QueryObject queryForExists(E entity) {
            SimpleQuery q = new SimpleQuery();
            q.append("select count(*) from ");
            q.append(tableName);
            q.append(" where ");

            StringJoiner j = new StringJoiner(" and ");
            for (Column id : idColumns) {
                j.add(id.getColumnName() + "=?");
                Object value = id.getValueForJdbc(entity);
                q.addArg(value);
            }
            q.append(j.toString());
            return q;
        }

        public Collection<Column> getAutogeneratedProperties() {
            List<Column> r = new ArrayList<>();
            for (Column c : idColumns) {
                if (c.getGenerator() != null) {
                    r.add(c);
                }
            }
            return r;
        }

        public Set<Column> getIdColumns() {
            return idColumns;
        }

        public void generateBeforeInsert(DataAccesFacade facade, E entity) {
            for (Column p : idColumns) {
                if (p.getGenerator() != null && p.getGenerator().generateBefore()) {
                    generate(facade, entity, p);
                }
            }
        }

        public void generateAfterInsert(DataAccesFacade facade, E entity) {
            for (Column p : idColumns) {
                if (p.getGenerator() != null && !p.getGenerator().generateBefore()) {
                    generate(facade, entity, p);
                }
            }
        }

        protected void generate(DataAccesFacade facade, E entity, Column p) {
            Generator g = p.getGenerator();
            Object value = g.generate(facade, p.getPropertyType());
            p.accessor.set(entity, value);
        }

    }

    public static class Column {

        final boolean id;
        final String columnName;

        private /* TODO */final Accessor accessor;

        final Handler handler;
        final Generator generator;

        public Column(boolean id, String columnName, Accessor accessor, Handler handler, Generator generator) {
            super();
            this.id = id;
            this.columnName = columnName;
            this.accessor = accessor;
            this.handler = handler;
            this.generator = generator;
        }

        public Object getValueForJdbc(Object entity) {
            Object javaValue = accessor.get(entity);
            return handler.getJdbcValue(javaValue);
        }

        public Object getValueForJdbc(Object entity, int propertyPathIndex) {
            Object javaValue = accessor.get(entity, propertyPathIndex);
            return handler.getJdbcValue(javaValue);
        }

        public void loadValue(Object entity, ResultSet rs) {
            try {
                Object value = handler.readValue(rs, columnName);
                accessor.set(entity, value);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }

        public boolean isId() {
            return id;
        }

        public String getPropertyName() {
            return accessor.getPropertyName();
        }

        public Class<?> getPropertyType() {
            return accessor.getPropertyFinalType();
        }

        public String getColumnName() {
            return columnName;
        }

        public Generator getGenerator() {
            return generator;
        }

    }
}
