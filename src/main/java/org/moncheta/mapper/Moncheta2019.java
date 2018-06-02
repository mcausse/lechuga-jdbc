package org.moncheta.mapper;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.StringJoiner;

import org.moncheta.jdbc.RowMapper;
import org.moncheta.jdbc.queryobject.QueryObject;
import org.moncheta.jdbc.queryobject.SimpleQuery;
import org.moncheta.mapper.autogen.Generator;
import org.moncheta.mapper.handler.Handler;

public class Moncheta2019 {

    public static class TableModel<E> {

        final Class<E> entityClass;
        final String tableName;

        final Set<Column> idColumns = new LinkedHashSet<>();
        final Set<Column> regularColumns = new LinkedHashSet<>();
        final Set<Column> allColumns = new LinkedHashSet<>();

        final org.moncheta.jdbc.RowMapper<E> rowMapper;

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
                    q.addArg(c.accessor.get(id, 1));
                }
                q.append(j.toString());
            }
            return q;
        }

        public QueryObject queryForLoadAll() {
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
            return q;
        }

        public QueryObject queryForInsert(Object id) {
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
                    q.addArg(c.accessor.get(id));
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
                    q.addArg(c.accessor.get(entity));
                }
                q.append(j.toString());
            }
            q.append(" where ");
            {
                StringJoiner j = new StringJoiner(" and ");
                for (Column c : idColumns) {
                    j.add(c.getColumnName() + "=?");
                    q.addArg(c.accessor.get(entity));
                }
                q.append(j.toString());
            }

            return q;
        }
    }

    public static class Column {

        final boolean id;
        final String columnName;

        final Accessor accessor;

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

    }
}
