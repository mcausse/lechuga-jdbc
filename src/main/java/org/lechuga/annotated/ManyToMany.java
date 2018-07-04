package org.lechuga.annotated;

import java.util.ArrayList;
import java.util.List;

import org.lechuga.jdbc.util.Pair;

public class ManyToMany<E, I, R> {

    final OneToMany<E, I> oneToMany;
    final ManyToOne<I, R> manyToOne;

    public ManyToMany(OneToMany<E, I> oneToMany, ManyToOne<I, R> manyToOne) {
        super();
        this.oneToMany = oneToMany;
        this.manyToOne = manyToOne;
    }

    public List<R> load(IEntityManagerFactory emf, E entity) {
        List<I> is = oneToMany.load(emf, entity);
        List<Pair<I, R>> pairs = manyToOne.load(emf, is);
        List<R> r = new ArrayList<>();
        pairs.forEach(p -> r.add(p.getRight()));
        return r;
    }

}