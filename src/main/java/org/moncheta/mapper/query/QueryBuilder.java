package org.moncheta.mapper.query;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.Map;

import org.moncheta.jdbc.DataAccesFacade;
import org.moncheta.jdbc.queryobject.QueryObject;
import org.moncheta.jdbc.queryobject.SimpleQuery;
import org.moncheta.mapper.EntityManager;
import org.moncheta.mapper.TableModel;

/**
 * <pre>
 * {ALIAS} -- evaluates as table name
 * {ALIAS ".*"} -- evaluates as all fields
 * {ALIAS "." PROP} -- evaluates as the column name of the corresponding property
 * {ALIAS "." PROP OP} -- evaluates as the column name of the corresponding property, with operation
 *
 *
 * final QueryBuilder q = new QueryBuilder();
 * q.addEm(&quot;d&quot;, new AnnotatedEntityManagerFactory(null).build(Dog.class).getRestrictions());
 * q.addEm(&quot;p&quot;, new AnnotatedEntityManagerFactory(null).build(Person.class).getRestrictions());
 *
 * q.append(&quot;select {d.*},{p.*} &quot;);
 * q.append(&quot;from {d} join {p} on {d.idPerson}={p.id} &quot;);
 * q.append(&quot;where {d.sex in (?,?)} &quot;, ESex.MALE, ESex.FEMALE);
 * q.append(&quot;and {d.name like ?} &quot;, ELike.CONTAINS.process(&quot;u&quot;));
 * q.append(&quot;and {p.age between ? and ?} &quot;, 1, 50);
 *
 * assertEquals(&quot;select DOG.ID,DOG.NAME,DOG.SEX,DOG.ID_PERSON,&quot; + &quot;PERSON.ID,PERSON.NAME,PERSON.AGE &quot;
 * 		+ &quot;from DOG join PERSON on DOG.ID_PERSON=PERSON.ID &quot; + &quot;where DOG.SEX in (?,?) &quot; + &quot;and DOG.NAME like ? &quot;
 * 		+ &quot;and PERSON.AGE between ? and ? &quot;
 * 		+ &quot; -- [MALE(String), FEMALE(String), %u%(String), 1(Integer), 50(Integer)]&quot;, q.getQueryObject()
 * 		.toString());
 * </pre>
 */
public class QueryBuilder<E> implements QueryObject {

    final DataAccesFacade facade;
    final TableModel<E> resultEntityModel;
    final String tableAlias;

    final SimpleQuery q;
    final Map<String, TableModel<?>> models;
    final QueryProcessor replacer;

    public QueryBuilder(DataAccesFacade facade, TableModel<E> resultEntityModel, String tableAlias) {
        super();
        this.facade = facade;
        this.resultEntityModel = resultEntityModel;
        this.tableAlias = tableAlias;
        this.q = new SimpleQuery();
        this.models = new LinkedHashMap<String, TableModel<?>>();
        this.replacer = new QueryProcessor();

        if (tableAlias != null) {
            this.models.put(tableAlias, resultEntityModel);
        }
    }

    public void addEm(final String alias, final TableModel<?> em) {
        this.models.put(alias, em);
    }

    public void addEm(final String alias, final EntityManager<?, ?> em) {
        this.models.put(alias, em.getModel());
    }

    // public void addEm(final String alias, final LentejaDao<?, ?> em) {
    // this.models.put(alias, em.getEntityManager().getTableModel());
    // }
    //
    // public TableModel<E> getResultEntityManager() {
    // return resultEntityModel;
    // }

    /**
     * <pre>
     *         {d} --- DOG d
     *         {d.*} --- d.ID,d.NAME,d.SEX,d.ID_PERSON
     *         {d.name=?} --- d.NAME=?
     *         {d.sex in (?,?)} --- d.SEX in (?,?)
     *         {d.name=?} or {d.name=?} --- d.NAME=? or d.NAME=?
     * </pre>
     */
    public void append(final String query, final Object... params) {
        q.append(replacer.process(models, query, params));
    }

    public QueryObject getQuery() {
        return q;
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

    public Map<String, TableModel<?>> getModels() {
        return models;
    }

    public Executor<E> getExecutor() {
        return new Executor<E>(facade, this, resultEntityModel.getRowMapper());
    }

}
