package org.moncheta.mapper.autogen;

import org.moncheta.jdbc.DataAccesFacade;
import org.moncheta.jdbc.RowMapper;
import org.moncheta.jdbc.ScalarMappers;
import org.moncheta.jdbc.exception.BaseException;
import org.moncheta.jdbc.exception.UnexpectedResultException;
import org.moncheta.jdbc.queryobject.InmutableQuery;
import org.moncheta.jdbc.queryobject.QueryObject;

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
