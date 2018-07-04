package org.lechuga.annotated.criteria;

import org.lechuga.jdbc.queryobject.Query;

public class CriterionImpl extends Query implements Criterion {

    public CriterionImpl() {
        super();
    }

    public CriterionImpl(String sqlFragment, Object... values) {
        super.append(sqlFragment);
        super.addArgs(values);
    }

    @Override
    public String toString() {
        return super.toString();
    }

}
