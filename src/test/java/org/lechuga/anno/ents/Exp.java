package org.lechuga.anno.ents;

import org.lechuga.annotated.anno.Column;
import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Table;
import org.lechuga.mapper.ents.ESex;
import org.lechuga.mapper.handler.custom.StringDateHandler;

@Table("exps")
public class Exp {

    ExpId id;

    @Column("text")
    String desc;

    @CustomHandler(value = StringDateHandler.class, args = "dd/MM/yyyy")
    String dataIni;

    @EnumHandler
    ESex sex;

    Fase fase;

    public Exp() {
        super();
    }

    public Exp(ExpId id, String desc, String dataIni, ESex sex, Fase fase) {
        super();
        this.id = id;
        this.desc = desc;
        this.dataIni = dataIni;
        this.sex = sex;
        this.fase = fase;
    }

    public ExpId getId() {
        return id;
    }

    public void setId(ExpId id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getDataIni() {
        return dataIni;
    }

    public void setDataIni(String dataIni) {
        this.dataIni = dataIni;
    }

    public ESex getSex() {
        return sex;
    }

    public void setSex(ESex sex) {
        this.sex = sex;
    }

    public Fase getFase() {
        return fase;
    }

    public void setFase(Fase fase) {
        this.fase = fase;
    }

    @Override
    public String toString() {
        return "Exp [id=" + id + ", desc=" + desc + ", dataIni=" + dataIni + ", sex=" + sex + ", fase=" + fase + "]";
    }

}