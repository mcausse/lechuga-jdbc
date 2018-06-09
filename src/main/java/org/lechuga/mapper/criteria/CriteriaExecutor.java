package org.lechuga.mapper.criteria;

import java.util.List;

import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.RowMapper;
import org.lechuga.jdbc.extractor.Pager;
import org.lechuga.jdbc.extractor.ResultSetPagedExtractor;
import org.lechuga.jdbc.queryobject.QueryObject;

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