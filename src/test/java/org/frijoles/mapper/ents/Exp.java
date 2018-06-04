package org.frijoles.mapper.ents;

public class Exp {

    ExpId id;
    String desc;
    EStatus status;

    public Exp() {
        super();
    }

    public Exp(ExpId id, String desc, EStatus status) {
        super();
        this.id = id;
        this.desc = desc;
        this.status = status;
    }

    public EStatus getStatus() {
        return status;
    }

    public void setStatus(EStatus status) {
        this.status = status;
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
        return "Exp [id=" + id + ", desc=" + desc + ", status=" + status + "]";
    }

}