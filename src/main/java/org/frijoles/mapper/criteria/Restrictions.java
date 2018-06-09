package org.frijoles.mapper.criteria;

import java.util.Arrays;
import java.util.List;
import java.util.StringJoiner;

import org.frijoles.mapper.Column;
import org.frijoles.mapper.Order;
import org.frijoles.mapper.TableModel;

public class Restrictions {

    final TableModel<?> em;
    final String alias;

    public Restrictions(TableModel<?> em, String alias) {
        super();
        this.em = em;
        this.alias = alias;
    }

    public Restrictions(TableModel<?> em) {
        this(em, null);
    }

    String aliaseColumn(String columnName) {
        if (alias == null) {
            return columnName;
        }
        return alias + "." + columnName;
    }

    public Criterion nativeSql(String nativeSql, Object... args) {
        return new CriterionImpl(nativeSql, args);
    }

    public Criterion star() {
        if (alias == null) {
            return new CriterionImpl(em.getTableName() + ".*");
        } else {
            return new CriterionImpl(alias + ".*");
        }
    }

    public Criterion table() {
        if (alias == null) {
            return new CriterionImpl(em.getTableName());
        } else {
            return new CriterionImpl(em.getTableName() + " " + alias);
        }
    }

    public Criterion all() {
        StringJoiner j = new StringJoiner(",");
        for (String c : em.getColumnNames()) {
            j.add(aliaseColumn(c));
        }
        return new CriterionImpl(j.toString());
    }

    public Criterion column(String propertyName) {
        return unaryOperator("", propertyName, "");
    }

    Criterion unaryOperator(String pre, String propertyName, String post) {
        Column c = em.findColumnByName(propertyName);
        return new CriterionImpl(pre + aliaseColumn(c.getColumnName()) + post);
    }

    Criterion binaryOperator(String propertyName, String op, Object value) {
        Column c = em.findColumnByName(propertyName);
        CriterionImpl q = new CriterionImpl();
        q.append(aliaseColumn(c.getColumnName()) + op + "?");
        q.addArg(c.convertValueForJdbc(value));
        return q;
    }

    Criterion binaryOperator(String propertyName1, String op, Restrictions rs2, String propertyName2) {
        CriterionImpl c = new CriterionImpl();
        Column c1 = em.findColumnByName(propertyName1);
        c.append(aliaseColumn(c1.getColumnName()));
        c.append(op);
        c.append(rs2.column(propertyName2));
        return c;
    }

    public Criterion isNull(String propertyName) {
        return unaryOperator("", propertyName, " is null");
    }

    public Criterion isNotNull(String propertyName) {
        return unaryOperator("", propertyName, " is not null");
    }

    public Criterion eq(String propertyName, Object value) {
        return binaryOperator(propertyName, "=", value);
    }

    public Criterion ne(String propertyName, Object value) {
        return binaryOperator(propertyName, "<>", value);
    }

    public Criterion le(String propertyName, Object value) {
        return binaryOperator(propertyName, "<=", value);
    }

    public Criterion ge(String propertyName, Object value) {
        return binaryOperator(propertyName, ">=", value);
    }

    public Criterion lt(String propertyName, Object value) {
        return binaryOperator(propertyName, "<", value);
    }

    public Criterion gt(String propertyName, Object value) {
        return binaryOperator(propertyName, ">", value);
    }

    //

    public Criterion eq(String propertyName1, Restrictions rs2, String propertyName2) {
        return binaryOperator(propertyName1, "=", rs2, propertyName2);
    }

    public Criterion ne(String propertyName1, Restrictions rs2, String propertyName2) {
        return binaryOperator(propertyName1, "<>", rs2, propertyName2);
    }

    public Criterion le(String propertyName1, Restrictions rs2, String propertyName2) {
        return binaryOperator(propertyName1, "<=", rs2, propertyName2);
    }

    public Criterion ge(String propertyName1, Restrictions rs2, String propertyName2) {
        return binaryOperator(propertyName1, ">=", rs2, propertyName2);
    }

    public Criterion lt(String propertyName1, Restrictions rs2, String propertyName2) {
        return binaryOperator(propertyName1, "<", rs2, propertyName2);
    }

    public Criterion gt(String propertyName1, Restrictions rs2, String propertyName2) {
        return binaryOperator(propertyName1, ">", rs2, propertyName2);
    }

    // ////

    static Criterion composition(String op, List<Criterion> qs) {
        CriterionImpl r = new CriterionImpl();
        for (int i = 0; i < qs.size(); i++) {
            if (i > 0) {
                r.append(op);
            }
            r.append(qs.get(i));
        }
        return r;
    }

    public static Criterion and(List<Criterion> qs) {
        return composition(" and ", qs);
    }

    public static Criterion or(List<Criterion> qs) {
        return composition(" or ", qs);
    }

    public static Criterion and(Criterion... qs) {
        return and(Arrays.asList(qs));
    }

    public static Criterion or(Criterion... qs) {
        return or(Arrays.asList(qs));
    }

    public static Criterion not(Criterion q) {
        CriterionImpl r = new CriterionImpl();
        r.append("not(");
        r.append(q);
        r.append(")");
        return r;
    }

    // ////

    public Criterion in(String propertyName, List<Object> values) {
        Column c = em.findColumnByName(propertyName);
        CriterionImpl r = new CriterionImpl();
        r.append(aliaseColumn(c.getColumnName()));
        r.append(" in (");
        for (int i = 0; i < values.size(); i++) {
            if (i > 0) {
                r.append(",");
            }
            r.append("?");
            r.addArg(c.convertValueForJdbc(values.get(i)));
        }
        r.append(")");
        return r;
    }

    public Criterion in(String propertyName, Object... values) {
        return in(propertyName, Arrays.asList(values));
    }

    public Criterion notIn(String propertyName, List<Object> values) {
        return not(in(propertyName, Arrays.asList(values)));
    }

    public Criterion notIn(String propertyName, Object... values) {
        return not(in(propertyName, Arrays.asList(values)));
    }

    public Criterion between(String propertyName, Object value1, Object value2) {
        Column c = em.findColumnByName(propertyName);
        CriterionImpl r = new CriterionImpl();
        r.append(aliaseColumn(c.getColumnName()));
        r.append(" between ? and ?");
        r.addArg(c.convertValueForJdbc(value1));
        r.addArg(c.convertValueForJdbc(value2));
        return r;
    }

    public Criterion like(String propertyName, ELike like, String value) {
        Column c = em.findColumnByName(propertyName);
        CriterionImpl r = new CriterionImpl();
        r.append(aliaseColumn(c.getColumnName()) + " like ?");
        r.addArg(like.process(value));
        return r;
    }

    public Criterion ilike(String propertyName, ELike like, String value) {
        Column c = em.findColumnByName(propertyName);
        CriterionImpl r = new CriterionImpl();
        r.append("upper(" + aliaseColumn(c.getColumnName()) + ") like upper(?)");
        r.addArg(like.process(value));
        return r;
    }

    public Criterion orderBy(Order... orders) {
        CriterionImpl r = new CriterionImpl();
        int c = 0;
        for (Order o : orders) {
            if (c > 0) {
                r.append(", ");
            }
            c++;

            Column column = em.findColumnByName(o.getProperty());
            r.append(aliaseColumn(column.getColumnName()) + o.getOrder());
        }
        return r;
    }

}
