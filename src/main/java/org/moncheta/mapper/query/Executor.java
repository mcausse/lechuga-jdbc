package org.moncheta.mapper.query;

import java.util.List;

import org.moncheta.jdbc.DataAccesFacade;
import org.moncheta.jdbc.RowMapper;
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

    // TODO
    // public void loadPage(Pager<E> pager) {
    // facade.loadPage(qo, rowMapper, pager);
    // }
}