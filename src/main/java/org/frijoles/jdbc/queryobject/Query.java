package org.frijoles.jdbc.queryobject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Query implements QueryObject {

    final StringBuilder query;
    final List<Object> args;

    public Query() {
        super();
        this.query = new StringBuilder();
        this.args = new ArrayList<Object>();
    }

    public static QueryObject immutable(String query, Object... args) {
        Query q = new Query();
        q.append(query);
        q.addArgs(args);
        return q;
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

    public void addArgsList(final Collection<?> params) {
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
    public Collection<Object> getArgsList() {
        return args;
    }

    @Override
    public String toString() {
        return QueryObjectUtils.toString(this);
    }

}
