package org.frijoles.mapper.query.criteria;

import java.util.Collection;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.RowMapper;
import org.frijoles.jdbc.extractor.ResultSetExtractor;
import org.frijoles.jdbc.queryobject.Query;
import org.frijoles.jdbc.queryobject.QueryObject;
import org.frijoles.mapper.EntityManager;

public class CriteriaBuilder implements QueryObject {

    final DataAccesFacade facade;
    final Query q = new Query();

    public CriteriaBuilder(DataAccesFacade facade) {
        super();
        this.facade = facade;
    }

    public void append(String format, QueryObject... qos) {

        int qi = 0;
        int i = 0;
        while (true) {
            int i2 = format.indexOf("{}", i);
            if (i2 < 0) {
                break;
            }
            q.append(format.substring(i, i2));
            q.append(qos[qi]);
            qi++;
            i = i2 + "{}".length();
        }
        q.append(format.substring(i));
    }

    @Override
    public String getSql() {
        return q.getSql();
    }

    @Override
    public Object[] getArgs() {
        return q.getArgs();
    }

    @Override
    public Collection<Object> getArgsList() {
        return q.getArgsList();
    }

    @Override
    public String toString() {
        return q.toString();
    }

    public <E> CriteriaExecutor<E> getExecutor(RowMapper<E> rowMapper) {
        return new CriteriaExecutor<E>(facade, rowMapper, q);
    }

    public <E> CriteriaExecutor<E> getExecutor(EntityManager<E, ?> em) {
        return new CriteriaExecutor<E>(facade, em.getModel().getRowMapper(), q);
    }

    public <T> T extract(ResultSetExtractor<T> extractor) {
        return facade.extract(q, extractor);
    }

}
