package typesafecriteria;

import java.util.Collection;

import org.lechuga.annotated.criteria.CriteriaExecutor;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.RowMapper;
import org.lechuga.jdbc.extractor.ResultSetExtractor;
import org.lechuga.jdbc.queryobject.Query;
import org.lechuga.jdbc.queryobject.QueryObject;
import org.lechuga.mapper.EntityManager;

public class CriteriaBuilder implements QueryObject {

    final DataAccesFacade facade;
    final EntityManagerFactory2 emf;
    final Query q = new Query();

    public CriteriaBuilder(DataAccesFacade facade, EntityManagerFactory2 emf) {
        super();
        this.facade = facade;
        this.emf = emf;
    }

    public void append(String format, QueryObject... qos) {
        // XXX el millor Formatter
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

    // @Deprecated
    public <E> CriteriaExecutor<E> getExecutor(EntityManager<E, ?> em) {
        return new CriteriaExecutor<E>(facade, em.getModel().getRowMapper(), q);
    }

    @SuppressWarnings("unchecked")
    public <E> CriteriaExecutor<E> getExecutor(Class<E> entityClass) {
        RowMapper<E> rowMapper = (RowMapper<E>) emf.getModelByEntityClass(entityClass).getRowMapper();
        return new CriteriaExecutor<E>(facade, rowMapper, q);
    }

    public <T> T extract(ResultSetExtractor<T> extractor) {
        return facade.extract(q, extractor);
    }

}
