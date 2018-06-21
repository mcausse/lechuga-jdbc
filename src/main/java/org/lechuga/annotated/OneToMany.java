package org.lechuga.annotated;

import java.util.ArrayList;
import java.util.List;

import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Criterion;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.mapper.Column;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.Order;

public class OneToMany<E, R> implements LoadableReference<E, List<R>> {

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
    @Override
    public List<R> load(IEntityManagerFactory emf, E entity) {

        Restrictions<E> rs = emf.getRestrictions(selfEntityClass, "s");
        Restrictions<R> rr = emf.getRestrictions(refEntityClass, "r");

        CriteriaBuilder c = emf.createCriteria();
        c.append("select {} from {} ", rr.all(), rr.table());
        c.append("join {} ", rs.table());

        List<Criterion> ons = new ArrayList<>();
        for (PropPair<E, R> m : mappings) {
            ons.add(rs.eq(m.left, rr, m.right));
        }

        c.append("on {} ", Restrictions.and(ons));

        List<Criterion> wheres = new ArrayList<>();
        EntityManager<E, Object> selfEm = emf.buildEntityManager(selfEntityClass);
        for (Column idColumn : selfEm.getModel().getIdColumns()) {
            wheres.add(rs.eq((MetaField<E, Object>) idColumn.getMetafield(), idColumn.getValueForJdbc(entity)));
        }
        c.append("where {} ", Restrictions.and(wheres));

        List<Order> orders = new ArrayList<>();
        EntityManager<R, Object> refEm = emf.buildEntityManager(refEntityClass);
        for (Column idColumn : refEm.getModel().getIdColumns()) {
            orders.add(Order.asc(idColumn.getMetafield()));
        }
        c.append("order by {} ", rr.orderBy(orders));

        return c.getExecutor(refEntityClass).load();
    }

}
