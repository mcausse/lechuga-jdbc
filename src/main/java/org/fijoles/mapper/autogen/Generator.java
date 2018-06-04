package org.fijoles.mapper.autogen;

import org.fijoles.jdbc.DataAccesFacade;
import org.fijoles.jdbc.RowMapper;
import org.fijoles.jdbc.ScalarMappers;
import org.fijoles.jdbc.exception.BaseException;
import org.fijoles.jdbc.exception.UnexpectedResultException;
import org.fijoles.jdbc.queryobject.InmutableQuery;
import org.fijoles.jdbc.queryobject.QueryObject;

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
