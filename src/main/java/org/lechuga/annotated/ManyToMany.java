package org.lechuga.annotated;

import java.util.ArrayList;
import java.util.List;

import org.lechuga.jdbc.util.Pair;

public class ManyToMany<E, I, R> {

    final OneToMany<E, I> oneToMany;
    final ManyToOne<I, R> manyToOne;

    protected ManyToMany(OneToMany<E, I> oneToMany, ManyToOne<I, R> manyToOne) {
        super();
        this.oneToMany = oneToMany;
        this.manyToOne = manyToOne;
    }

    public List<Pair<E, List<R>>> load(IEntityManagerFactory emf, List<E> entities) {
        List<Pair<E, List<R>>> r = new ArrayList<>();
        for (E entity : entities) {
            r.add(new Pair<>(entity, load(emf, entity)));
        }
        return r;
    }

    public List<R> load(IEntityManagerFactory emf, E entity) {
        List<I> is = oneToMany.load(emf, entity);
        List<Pair<I, R>> pairs = manyToOne.load(emf, is);
        List<R> r = new ArrayList<>();
        pairs.forEach(p -> r.add(p.getRight()));
        return r;
    }

    @Override
    public String toString() {
        return "ManyToMany [oneToMany=" + oneToMany + ", manyToOne=" + manyToOne + "]";
    }

}