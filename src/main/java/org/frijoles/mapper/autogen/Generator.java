package org.frijoles.mapper.autogen;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.RowMapper;
import org.frijoles.jdbc.ScalarMappers;
import org.frijoles.jdbc.exception.BaseException;
import org.frijoles.jdbc.exception.UnexpectedResultException;
import org.frijoles.jdbc.queryobject.InmutableQuery;
import org.frijoles.jdbc.queryobject.QueryObject;

public abstract class Generator {

    final QueryObject q;

    public Generator(String query) {
        super();
        this.q = new InmutableQuery(query);
    }

    public Object generate(DataAccesFacade facade, Class<?> columnClass) {
        RowMapper<?> rowMapper = ScalarMappers.getScalarMapperFor(columnClass);
        try {
            return facade.loadUnique(q, rowMapper);
        } catch (UnexpectedResultException e) {
            throw new BaseException("error running incrementer: " + toString(), e);
        }
    }

    public abstract boolean generateBefore();

    @Override
    public String toString() {
        return getClass().getName() + "; query=" + q;
    }

}
