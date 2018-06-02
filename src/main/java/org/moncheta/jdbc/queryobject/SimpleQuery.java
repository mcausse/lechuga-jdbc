package org.moncheta.jdbc.queryobject;

import java.util.ArrayList;
import java.util.List;

public class SimpleQuery implements QueryObject {

    final StringBuilder query;
    final List<Object> args;

    public SimpleQuery() {
        super();
        this.query = new StringBuilder();
        this.args = new ArrayList<Object>();
    }

    // ---------------------------------------------------

    public void append(final String sql) {
        query.append(sql);
    }

    public void append(final QueryObject query) {
        append(query.getSql());
        addArgs(query.getArgs());
    }

    public void addArg(final Object param) {
        this.args.add(param);
    }

    public void addArgs(final Object... params) {
        for (Object p : params) {
            addArg(p);
        }
    }

    public void addArgsList(final List<?> params) {
        this.args.addAll(params);
    }

    // ---------------------------------------------------

    @Override
    public String getSql() {
        return query.toString();
    }

    @Override
    public Object[] getArgs() {
        return args.toArray();
    }

    @Override
    public String toString() {
        return QueryObjectUtils.toString(this);
    }

}
