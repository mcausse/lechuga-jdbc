package org.moncheta;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.moncheta.mapper.Accessor;

public class AccessorTest {

    @Test
    public void testName() throws Exception {

        Accessor anyExp = new Accessor(Exp.class, "id.anyExp");
        Accessor numExp = new Accessor(Exp.class, "id.numExp");
        Accessor desc = new Accessor(Exp.class, "desc");

        Exp e = new Exp();
        anyExp.set(e, 2018);
        numExp.set(e, 123L);
        desc.set(e, "alo");

        assertEquals("Exp [id=IdExp [anyExp=2018, numExp=123], desc=alo]", e.toString());

        assertEquals(2018, anyExp.get(e));
        assertEquals(123L, numExp.get(e));
        assertEquals("alo", desc.get(e));
    }

    public static class ExpId {
        Integer anyExp;
        Long numExp;

        public ExpId() {
            super();
        }

        public ExpId(Integer anyExp, Long numExp) {
            super();
            this.anyExp = anyExp;
            this.numExp = numExp;
        }

        public Integer getAnyExp() {
            return anyExp;
        }

        public void setAnyExp(Integer anyExp) {
            this.anyExp = anyExp;
        }

        public Long getNumExp() {
            return numExp;
        }

        public void setNumExp(Long numExp) {
            this.numExp = numExp;
        }

        @Override
        public String toString() {
            return "IdExp [anyExp=" + anyExp + ", numExp=" + numExp + "]";
        }

    }

    public static class Exp {

        ExpId id;
        String desc;

        public Exp() {
            super();
        }

        public Exp(ExpId id, String desc) {
            super();
            this.id = id;
            this.desc = desc;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        public ExpId getId() {
            return id;
        }

        public void setId(ExpId id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "Exp [id=" + id + ", desc=" + desc + "]";
        }

    }
}
