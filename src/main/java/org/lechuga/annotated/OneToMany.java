package org.lechuga.annotated;

import java.util.ArrayList;
import java.util.List;

import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Criterion;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.mapper.Column;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.Order;

public class OneToMany<E, R> {

    final Class<E> selfEntityClass;
    final Class<R> refEntityClass;
    final PropPair<E, R>[] mappings;

    @SafeVarargs
    public OneToMany(Class<E> selfEntityClass, Class<R> refEntityClass, PropPair<E, R>... mappings) {
        super();
        this.selfEntityClass = selfEntityClass;
        this.refEntityClass = refEntityClass;
        this.mappings = mappings;
    }

    @SuppressWarnings("unchecked")
    public List<R> load(IEntityManagerFactory emf, E entity, Order... orders) {

        Restrictions<R> rr = emf.getRestrictions(refEntityClass, "r");

        CriteriaBuilder c = emf.createCriteria();
        c.append("select {} from {} ", rr.all(), rr.table());

        List<Criterion> wheres = new ArrayList<>();
        EntityManager<E, Object> selfEm = emf.getEntityManager(selfEntityClass);

        for (PropPair<E, R> m : mappings) {
            Column selfColumn = selfEm.getModel().findColumnByMetaField(m.left);
            Object selfValue = selfColumn.getValueForJdbc(entity);
            wheres.add(rr.eq((MetaField<R, Object>) m.right, selfValue));
        }

        c.append("where {} ", Restrictions.and(wheres));

        if (orders.length > 0) {
            c.append("order by {} ", rr.orderBy(orders));
        }

        return c.getExecutor(refEntityClass).load();
    }

}
