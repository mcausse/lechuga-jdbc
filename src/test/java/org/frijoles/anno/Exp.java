package org.frijoles.anno;

import org.fijoles.annotated.anno.Column;
import org.fijoles.annotated.anno.CustomHandler;
import org.fijoles.annotated.anno.EnumHandler;
import org.fijoles.annotated.anno.Table;
import org.fijoles.mapper.handler.custom.StringDateHandler;
import org.frijoles.mapper.ents.ESex;

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