package org.frijoles.mapper;

import static org.junit.Assert.assertEquals;

import org.frijoles.mapper.ents.EStatus;
import org.frijoles.mapper.ents.Exp;
import org.junit.Test;

public class AccessorTest {

    @Test
    public void testName() throws Exception {

        Accessor anyExp = new Accessor(Exp.class, "id.anyExp");
        Accessor numExp = new Accessor(Exp.class, "id.numExp");
        Accessor desc = new Accessor(Exp.class, "desc");
        Accessor status = new Accessor(Exp.class, "status");

        Exp e = new Exp();
        anyExp.set(e, 2018);
        numExp.set(e, 123L);
        desc.set(e, "alo");
        status.set(e, EStatus.ACTIVE);

        assertEquals("Exp [id=IdExp [anyExp=2018, numExp=123], desc=alo, status=ACTIVE]", e.toString());

        assertEquals(2018, anyExp.get(e));
        assertEquals(123L, numExp.get(e));
        assertEquals("alo", desc.get(e));
        assertEquals(EStatus.ACTIVE, status.get(e));
    }

}
