package org.frijoles.mapper.criteria;

import java.util.List;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.RowMapper;
import org.frijoles.jdbc.extractor.Pager;
import org.frijoles.jdbc.extractor.ResultSetPagedExtractor;
import org.frijoles.jdbc.queryobject.QueryObject;

public class CriteriaExecutor<E> {

    final DataAccesFacade facade;
    final RowMapper<E> rowMapper;
    final QueryObject qo;

    public CriteriaExecutor(DataAccesFacade facade, RowMapper<E> rowMapper, QueryObject qo) {
        super();
        this.facade = facade;
        this.rowMapper = rowMapper;
        this.qo = qo;
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

}