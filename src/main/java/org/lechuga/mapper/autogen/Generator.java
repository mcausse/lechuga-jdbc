package org.lechuga.mapper.autogen;

import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.RowMapper;
import org.lechuga.jdbc.ScalarMappers;
import org.lechuga.jdbc.exception.LechugaException;
import org.lechuga.jdbc.exception.UnexpectedResultException;
import org.lechuga.jdbc.queryobject.Query;
import org.lechuga.jdbc.queryobject.QueryObject;

public abstract class Generator {

    final QueryObject q;

    public Generator(String query) {
        super();
        this.q = Query.immutable(query);
    }

    public Object generate(DataAccesFacade facade, Class<?> columnClass) {
        RowMapper<?> rowMapper = ScalarMappers.getScalarMapperFor(columnClass);
        try {
            return facade.loadUnique(q, rowMapper);
        } catch (UnexpectedResultException e) {
            throw new LechugaException("error running incrementer: " + toString(), e);
        }
    }

    public abstract boolean generateBefore();

    @Override
    public String toString() {
        return getClass().getName() + "; query=" + q;
    }

}
