package org.frijoles.mapper.query;

import java.util.List;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.RowMapper;
import org.frijoles.jdbc.extractor.Pager;
import org.frijoles.jdbc.extractor.ResultSetExtractor;
import org.frijoles.jdbc.extractor.ResultSetPagedExtractor;
import org.frijoles.jdbc.queryobject.QueryObject;

public class Executor<E> {

    final DataAccesFacade facade;
    final QueryObject qo;
    final RowMapper<E> rowMapper;

    public Executor(DataAccesFacade facade, QueryObject qo, RowMapper<E> rowMapper) {
        super();
        this.facade = facade;
        this.qo = qo;
        this.rowMapper = rowMapper;
    }

    public int update() {
        return facade.update(qo);
    }

    public E loadUnique() {
        return facade.loadUnique(qo, rowMapper);
    }

    public List<E> load() {
        return facade.load(qo, rowMapper);
    }

    public <T> T loadUnique(RowMapper<T> rowMapper) {
        return facade.loadUnique(qo, rowMapper);
    }

    public <T> List<T> load(RowMapper<T> rowMapper) {
        return facade.load(qo, rowMapper);
    }

    public <T> T extract(ResultSetExtractor<T> extractor) {
        return facade.extract(qo, extractor);
    }

    public Pager<E> loadPage(int pageSize, int numPage) {
        return facade.extract(qo, new ResultSetPagedExtractor<E>(rowMapper, pageSize, numPage));
    }

}