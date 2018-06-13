package org.lechuga.anno.ents;

import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.mapper.autogen.HsqldbSequence;

public class ExpId {

    @Id
    Integer anyExp;

    @Id
    @Generated(value = HsqldbSequence.class, args = "seq_exp")
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
        return "ExpId [anyExp=" + anyExp + ", numExp=" + numExp + "]";
    }

}