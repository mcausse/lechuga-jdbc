package org.lechuga.mapper;

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

import org.lechuga.annotated.MetaField;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.RowMapper;
import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.jdbc.queryobject.Query;
import org.lechuga.jdbc.queryobject.QueryObject;
import org.lechuga.mapper.autogen.Generator;

public class TableModel<E> {

    final Class<E> entityClass;
    final Class<?> metaModelClass;
    final String tableName;

    final Set<Column> idColumns = new LinkedHashSet<>();
    final Set<Column> regularColumns = new LinkedHashSet<>();
    final Set<Column> allColumns = new LinkedHashSet<>();
    final Map<String, Column> propsMap = new LinkedHashMap<>();

    final RowMapper<E> rowMapper;

    public TableModel(Class<E> entityClass, Class<?> metaModelClass, String tableName, Collection<Column> columns) {
        super();
        this.entityClass = entityClass;
        this.metaModelClass = metaModelClass;
        this.tableName = tableName;

        for (Column column : columns) {
            if (column.isId()) {
                idColumns.add(column);
            } else {
                regularColumns.add(column);
            }
            allColumns.add(column);
            propsMap.put(column.getPropertyName(), column);
        }

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
            throw new LechugaException("property not found: " + entityClass.getName() + "#" + name);
        }
        return this.propsMap.get(name);
    }

    public Column findColumnByMetaField(MetaField<?, ?> metaField) {
        if (!this.propsMap.containsKey(metaField.getPropertyName())) {
            throw new LechugaException(
                    "property not found: " + entityClass.getName() + "#" + metaField.getPropertyName());
        }
        if (this.propsMap.get(metaField.getPropertyName()).getMetafield() != metaField) {
            throw new LechugaException("this meta-field is not of meta-model: meta-model=" + metaModelClass.getName()
                    + "; meta-field=" + metaField);
        }
        Column col = this.propsMap.get(metaField.getPropertyName());
        return col;
    }

    protected String orderBy(Order[] orders) {
        StringJoiner r = new StringJoiner(",");
        for (Order o : orders) {
            Column c = findColumnByName(o.getMetaField().getPropertyName());
            r.add(c.getColumnName() + o.getOrder());
        }
        return " order by " + r.toString();
    }

    public QueryObject queryForLoadById(Object id) {
        Query q = new Query();
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
        Query q = new Query();
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
        if (orders.length > 0) {
            q.append(orderBy(orders));
        }
        return q;
    }

    public QueryObject queryForInsert(E entity) {
        Query q = new Query();
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
        Query q = new Query();
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

    public QueryObject queryForUpdate(E entity, String... propertyNames) {
        Query q = new Query();
        q.append("update ");
        q.append(tableName);
        q.append(" set ");
        {
            StringJoiner j = new StringJoiner(",");
            for (String propName : propertyNames) {
                Column c = findColumnByName(propName);
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
        Query q = new Query();
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
        Query q = new Query();
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
        Query q = new Query();
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

        q.addArg(c.convertValueForJdbc(value));

        if (orders.length > 0) {
            q.append(orderBy(orders));
        }
        return q;
    }

    public QueryObject queryExists(E entity) {
        Query q = new Query();
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

    public QueryObject queryExistsById(Object id) {
        Query q = new Query();
        q.append("select count(*) from ");
        q.append(tableName);
        q.append(" where ");

        StringJoiner j = new StringJoiner(" and ");
        for (Column i : idColumns) {
            j.add(i.getColumnName() + "=?");
            Object value = i.getValueForJdbc(id, 1);
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
        // FIXME no es fa tranformació de tipus bean<=>jdbc: si és així un autogenerated
        // no pot tenir @CustomHandler/@EnumHandler => normal, es genera amb un
        // ScalarMapper, no pot haver-hi transformació, no?
        Object value = g.generate(facade, p.getPropertyType());
        p.accessor.set(entity, value);
    }

    public Collection<String> getColumnNames() {
        List<String> r = new ArrayList<>();
        for (Column c : allColumns) {
            r.add(c.getColumnName());
        }
        return r;
    }

    public Map<String, Column> getPropsMap() {
        return propsMap;
    }

    @Override
    public String toString() {
        return "TableModel [entityClass=" + entityClass.getName() + ", tableName=" + tableName + "]";
    }

}
