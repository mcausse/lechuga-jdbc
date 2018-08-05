package udb2019;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SList<K extends Comparable<K>> {

    public static void main(String[] args) {
        SList<Integer> l = new SList<>(new ArrayList<>(Arrays.asList(1, 2, 5, 6, 7, 9)));
        System.out.println(l.find(8));
        System.out.println(l.find(9));
        System.out.println(l);
        l.add(4);
        l.add(3);
        l.add(8);
        System.out.println(l);
    }

    final List<K> list;

    public SList() {
        super();
        this.list = new ArrayList<>();
    }

    public SList(List<K> list) {
        super();
        this.list = list;
    }

    public static class SearchResult {

        final boolean found;
        final int position;

        public SearchResult(boolean found, int position) {
            super();
            this.found = found;
            this.position = position;
        }

        public boolean isFound() {
            return found;
        }

        public int getPosition() {
            return position;
        }

        @Override
        public String toString() {
            return "SearchResult [found=" + found + ", position=" + position + "]";
        }

    }

    public SearchResult find(K t) {

        // Asignar 0 a L y a R (n − 1).
        int l = 0;
        int r = list.size() - 1;
        // Si L > R, la búsqueda termina sin encontrar el valor.
        while (l <= r) {
            // Sea m (la posición del elemento del medio) igual a la parte entera de (L + R)
            // / 2.
            int m = (l + r) / 2;
            // Si Am < T, igualar L a m + 1 e ir al paso 2.
            if (ComparableUtils.lt(list.get(m), t)) {
                l = m + 1;
            } else
            // Si Am > T, igualar R a m – 1 e ir al paso 2.
            if (ComparableUtils.gt(list.get(m), t)) {
                r = m - 1;
            } else
            // Si Am = T, la búsqueda terminó, retornar m.
            if (ComparableUtils.eq(list.get(m), t)) {
                return new SearchResult(true, m);
            }
        }
        return new SearchResult(false, l);
    }

    public void add(K k) {
        SearchResult f = find(k);
        if (f.isFound()) {
            list.set(f.getPosition(), k);
        } else {
            list.add(f.getPosition(), k);
        }
    }

    @Override
    public String toString() {
        return list.toString();
    }

}
