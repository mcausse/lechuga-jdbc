package org.lechuga.annotated;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Criterion;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.RowMapper;
import org.lechuga.jdbc.extractor.ResultSetExtractor;
import org.lechuga.jdbc.util.Pair;
import org.lechuga.mapper.Column;
import org.lechuga.mapper.EntityManager;

public class ManyToOne<E, R> {

    protected final Class<E> selfEntityClass;
    protected final Class<R> refEntityClass;
    protected final PropPair<E, R, ?>[] mappings;

    @SafeVarargs
    public ManyToOne(Class<E> selfEntityClass, Class<R> refEntityClass, PropPair<E, R, ?>... mappings) {
        super();
        this.selfEntityClass = selfEntityClass;
        this.refEntityClass = refEntityClass;
        this.mappings = mappings;
    }

    @SuppressWarnings("unchecked")
    public List<Pair<E, R>> load(IEntityManagerFactory emf, Collection<E> entities) {
        // List<Pair<E, R>> r = new ArrayList<>();
        // for (E entity : entities) {
        // // XXX cada volta és una query!
        // r.add(new Pair<>(entity, load(emf, entity)));
        // }
        // return r;

        EntityManager<E, Object> selfEm = emf.getEntityManager(selfEntityClass);
        EntityManager<R, Object> refEm = emf.getEntityManager(refEntityClass);

        Restrictions<E> er = emf.getRestrictions(selfEntityClass, "e");
        Restrictions<R> rr = emf.getRestrictions(refEntityClass, "r");

        CriteriaBuilder c = emf.createCriteria();
        c.append("select {},{} ", er.all(), rr.all());
        c.append("from {} join {} ", er.table(), rr.table());

        {
            List<Criterion> ons = new ArrayList<>();
            for (PropPair<E, R, ?> m : mappings) {
                Column selfColumn = selfEm.getModel().findColumnByMetaField(m.left);
                Column refColumn = refEm.getModel().findColumnByMetaField(m.right);
                MetaField<E, ?> selfMeta = (MetaField<E, ?>) selfColumn.getMetafield();
                MetaField<R, ?> refMeta = (MetaField<R, ?>) refColumn.getMetafield();
                ons.add(er.eq(selfMeta, rr, refMeta));
            }
            c.append("on {} ", Restrictions.and(ons));
        }

        {
            List<Criterion> ors = new ArrayList<>();
            for (E entity : entities) {
                List<Criterion> ands = new ArrayList<>();

                for (Column m : selfEm.getModel().getIdColumns()) {
                    MetaField<E, Object> metaf = (MetaField<E, Object>) m.getMetafield();
                    ands.add(er.eq(metaf, m.getValueForJdbc(entity)));
                }

                ors.add(Restrictions.and(ands));
            }
            c.append("where {} ", Restrictions.or(ors));
        }

        return c.getExecutor(refEntityClass).extract(new ResultSetExtractor<List<Pair<E, R>>>() {

            final RowMapper<E> emapper = emf.getEntityManager(selfEntityClass).getModel().getRowMapper();
            final RowMapper<R> rmapper = emf.getEntityManager(refEntityClass).getModel().getRowMapper();

            @Override
            public List<Pair<E, R>> extract(ResultSet rs) throws SQLException {
                List<Pair<E, R>> rr = new ArrayList<>();
                while (rs.next()) {
                    E e = emapper.mapRow(rs);
                    R r = rmapper.mapRow(rs);
                    rr.add(new Pair<>(e, r));
                }
                return rr;
            }
        });
    }

    @SuppressWarnings("unchecked")
    public R load(IEntityManagerFactory emf, E entity) {

        Restrictions<R> rr = emf.getRestrictions(refEntityClass, "r");

        CriteriaBuilder c = emf.createCriteria();
        c.append("select {} from {} ", rr.all(), rr.table());

        List<Criterion> wheres = new ArrayList<>();
        EntityManager<E, Object> selfEm = emf.getEntityManager(selfEntityClass);

        for (PropPair<E, R, ?> m : mappings) {
            Column selfColumn = selfEm.getModel().findColumnByMetaField(m.left);
            Object selfValue = selfColumn.getValueForJdbc(entity);
            wheres.add(rr.eq((MetaField<R, Object>) m.right, selfValue));
        }

        c.append("where {} ", Restrictions.and(wheres));

        return c.getExecutor(refEntityClass).loadUnique();
    }

    @Override
    public String toString() {
        return "ManyToOne [" + selfEntityClass.getName() + " => " + refEntityClass.getName() + ", mappings="
                + Arrays.toString(mappings) + "]";
    }

}