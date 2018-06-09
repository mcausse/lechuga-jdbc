package org.frijoles.mapper.autogen;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.RowMapper;
import org.frijoles.jdbc.ScalarMappers;
import org.frijoles.jdbc.exception.FrijolesException;
import org.frijoles.jdbc.exception.UnexpectedResultException;
import org.frijoles.jdbc.queryobject.Query;
import org.frijoles.jdbc.queryobject.QueryObject;

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
            throw new FrijolesException("error running incrementer: " + toString(), e);
        }
    }

    public abstract boolean generateBefore();

    @Override
    public String toString() {
        return getClass().getName() + "; query=" + q;
    }

}
