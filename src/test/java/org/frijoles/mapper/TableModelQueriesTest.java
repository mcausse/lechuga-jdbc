package org.frijoles.mapper;

import org.frijoles.mapper.ents.EStatus;
import org.frijoles.mapper.ents.Exp;
import org.frijoles.mapper.ents.ExpId;
import org.frijoles.mapper.handler.EnumeratedHandler;
import org.frijoles.mapper.handler.Handlers;
import org.junit.Test;

public class TableModelQueriesTest {

    @Test
    public void testName() throws Exception {

        TableModel<Exp> p = new TableModel<>(Exp.class, "exps");
        p.addColumn(new Column(true, "any_exp", new Accessor(Exp.class, "id.anyExp"), Handlers.INTEGER, null));
        p.addColumn(new Column(true, "num_exp", new Accessor(Exp.class, "id.numExp"), Handlers.LONG, null));
        p.addColumn(new Column(false, "desc", new Accessor(Exp.class, "desc"), Handlers.STRING, null));
        p.addColumn(new Column(false, "status", new Accessor(Exp.class, "status"), new EnumeratedHandler(EStatus.class),
                null));

        ExpId id = new ExpId(2019, 123L);
        Exp e = new Exp(id, "alo", EStatus.ACTIVE);

        System.out.println(p.queryForLoadById(id));
        System.out.println(p.queryForLoadAll(new Order[] { Order.asc("id.anyExp"), Order.asc("desc") }));
        System.out.println(p.queryForInsert(e));
        System.out.println(p.queryForUpdate(e));
        System.out.println(p.queryForDelete(e));
        System.out.println(p.queryForDeleteById(id));

        System.out.println(p.queryForLoadByProp("id.numExp", 123L, new Order[] {}));
        System.out.println(p.queryForLoadByProp("status", EStatus.INACTIVE, new Order[] {}));

        System.out.println(p.queryForUpdate(e, new String[] { "status" }));
    }
}
