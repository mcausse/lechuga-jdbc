package org.frijoles.mapper.query.criteria;

import org.frijoles.jdbc.queryobject.Query;

public class CriterionImpl extends Query implements Criterion {

    public CriterionImpl() {
        super();
    }

    public CriterionImpl(String sqlFragment, Object... values) {
        super.append(sqlFragment);
        super.addArgs(values);
    }

}
