package org.moncheta.mapper.query;

import java.util.List;

import org.moncheta.jdbc.DataAccesFacade;
import org.moncheta.jdbc.RowMapper;
import org.moncheta.jdbc.extractor.Pager;
import org.moncheta.jdbc.extractor.ResultSetExtractor;
import org.moncheta.jdbc.extractor.ResultSetPagedExtractor;
import org.moncheta.jdbc.queryobject.QueryObject;

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

    public Pager<E> loadPage(int pageSize, int numPage) {
        return facade.extract(qo, new ResultSetPagedExtractor<E>(rowMapper, pageSize, numPage));
    }

    public <T> T extract(ResultSetExtractor<T> extractor) {
        return facade.extract(qo, extractor);
    }
}