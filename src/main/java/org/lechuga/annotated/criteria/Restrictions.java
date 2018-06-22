package org.lechuga.annotated.criteria;

import java.util.Arrays;
import java.util.Collection;
import java.util.StringJoiner;

import org.lechuga.annotated.MetaField;
import org.lechuga.mapper.Column;
import org.lechuga.mapper.Order;
import org.lechuga.mapper.TableModel;

public class Restrictions<E> {

    final TableModel<E> em;
    final String alias;

    public Restrictions(TableModel<E> em, String alias) {
        super();
        this.em = em;
        this.alias = alias;
    }

    public Restrictions(TableModel<E> em) {
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

    public Criterion column(MetaField<E, ?> metaField) {
        return unaryOperator("", metaField, "");
    }

    Criterion unaryOperator(String pre, MetaField<E, ?> metaField, String post) {
        Column c = em.findColumnByMetaField(metaField);
        return new CriterionImpl(pre + aliaseColumn(c.getColumnName()) + post);
    }

    <T> Criterion binaryOperator(MetaField<E, T> metaField, String op, T value) {
        Column c = em.findColumnByMetaField(metaField);
        CriterionImpl q = new CriterionImpl();
        q.append(aliaseColumn(c.getColumnName()) + op + "?");
        q.addArg(c.convertValueForJdbc(value));
        return q;
    }

    <E2> Criterion binaryOperator(MetaField<E, ?> metaField1, String op, Restrictions<E2> rs2,
            MetaField<E2, ?> metaField2) {
        CriterionImpl c = new CriterionImpl();
        Column c1 = em.findColumnByMetaField(metaField1);
        c.append(aliaseColumn(c1.getColumnName()));
        c.append(op);
        c.append(rs2.column(metaField2));
        return c;
    }

    public Criterion isNull(MetaField<E, ?> metaField) {
        return unaryOperator("", metaField, " is null");
    }

    public Criterion isNotNull(MetaField<E, ?> metaField) {
        return unaryOperator("", metaField, " is not null");
    }

    public <T> Criterion eq(MetaField<E, T> metaField, T value) {
        return binaryOperator(metaField, "=", value);
    }

    public <T> Criterion ne(MetaField<E, T> metaField, T value) {
        return binaryOperator(metaField, "<>", value);
    }

    public <T> Criterion le(MetaField<E, T> metaField, T value) {
        return binaryOperator(metaField, "<=", value);
    }

    public <T> Criterion ge(MetaField<E, T> metaField, T value) {
        return binaryOperator(metaField, ">=", value);
    }

    public <T> Criterion lt(MetaField<E, T> metaField, T value) {
        return binaryOperator(metaField, "<", value);
    }

    public <T> Criterion gt(MetaField<E, T> metaField, T value) {
        return binaryOperator(metaField, ">", value);
    }

    //

    public <E2> Criterion eq(MetaField<E, ?> metaField1, Restrictions<E2> rs2, MetaField<E2, ?> metaField2) {
        return binaryOperator(metaField1, "=", rs2, metaField2);
    }

    public <E2> Criterion ne(MetaField<E, ?> metaField1, Restrictions<E2> rs2, MetaField<E2, ?> metaField2) {
        return binaryOperator(metaField1, "<>", rs2, metaField2);
    }

    public <E2> Criterion le(MetaField<E, ?> metaField1, Restrictions<E2> rs2, MetaField<E2, ?> metaField2) {
        return binaryOperator(metaField1, "<=", rs2, metaField2);
    }

    public <E2> Criterion ge(MetaField<E, ?> metaField1, Restrictions<E2> rs2, MetaField<E2, ?> metaField2) {
        return binaryOperator(metaField1, ">=", rs2, metaField2);
    }

    public <E2> Criterion lt(MetaField<E, ?> metaField1, Restrictions<E2> rs2, MetaField<E2, ?> metaField2) {
        return binaryOperator(metaField1, "<", rs2, metaField2);
    }

    public <E2> Criterion gt(MetaField<E, ?> metaField1, Restrictions<E2> rs2, MetaField<E2, ?> metaField2) {
        return binaryOperator(metaField1, ">", rs2, metaField2);
    }

    // ////

    static Criterion composition(String op, Collection<Criterion> qs) {
        CriterionImpl r = new CriterionImpl();
        int i = 0;
        for (Criterion q : qs) {
            if (i > 0) {
                r.append(op);
            }
            r.append(q);
            i++;
        }
        return r;
    }

    public static Criterion and(Collection<Criterion> qs) {
        return composition(" and ", qs);
    }

    public static Criterion or(Collection<Criterion> qs) {
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

    public <T> Criterion in(MetaField<E, T> metaField, Collection<T> values) {
        Column c = em.findColumnByMetaField(metaField);
        CriterionImpl r = new CriterionImpl();
        r.append(aliaseColumn(c.getColumnName()));
        r.append(" in (");
        int i = 0;
        for (Object v : values) {
            if (i > 0) {
                r.append(",");
            }
            r.append("?");
            r.addArg(c.convertValueForJdbc(v));
            i++;
        }
        r.append(")");
        return r;
    }

    @SuppressWarnings("unchecked")
    public <T> Criterion in(MetaField<E, T> metaField, T... values) {
        return in(metaField, Arrays.asList(values));
    }

    public <T> Criterion notIn(MetaField<E, T> metaField, Collection<T> values) {
        return not(in(metaField, values));
    }

    @SuppressWarnings("unchecked")
    public <T> Criterion notIn(MetaField<E, T> metaField, T... values) {
        return not(in(metaField, Arrays.asList(values)));
    }

    public <T> Criterion between(MetaField<E, T> metaField, T value1, T value2) {
        Column c = em.findColumnByMetaField(metaField);
        CriterionImpl r = new CriterionImpl();
        r.append(aliaseColumn(c.getColumnName()));
        r.append(" between ? and ?");
        r.addArg(c.convertValueForJdbc(value1));
        r.addArg(c.convertValueForJdbc(value2));
        return r;
    }

    public Criterion like(MetaField<E, String> metaField, ELike like, String value) {
        Column c = em.findColumnByMetaField(metaField);
        CriterionImpl r = new CriterionImpl();
        r.append(aliaseColumn(c.getColumnName()) + " like ?");
        r.addArg(like.process(value));
        return r;
    }

    public Criterion ilike(MetaField<E, String> metaField, ELike like, String value) {
        Column c = em.findColumnByMetaField(metaField);
        CriterionImpl r = new CriterionImpl();
        r.append("upper(" + aliaseColumn(c.getColumnName()) + ") like upper(?)");
        r.addArg(like.process(value));
        return r;
    }

    public Criterion orderBy(Collection<Order<E>> orders) {
        CriterionImpl r = new CriterionImpl();
        int c = 0;
        for (Order<E> o : orders) {
            if (c > 0) {
                r.append(", ");
            }
            c++;

            Column column = em.findColumnByMetaField(o.getMetaField());
            r.append(aliaseColumn(column.getColumnName()) + o.getOrder());
        }
        return r;
    }

}
