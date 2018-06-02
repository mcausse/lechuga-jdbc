package org.moncheta.jdbc.queryobject;

public class InmutableQuery implements QueryObject {

    final String sql;
    final Object[] args;

    public InmutableQuery(String sql, Object... args) {
        super();
        this.sql = sql;
        this.args = args;
    }

    @Override
    public String getSql() {
        return sql;
    }

    @Override
    public Object[] getArgs() {
        return args;
    }

    @Override
    public String toString() {
        return QueryObjectUtils.toString(this);
    }

}
