package org.moncheta.ents;

public class ExpId {

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
