package linenetcom.ent;

import org.lechuga.annotated.anno.Table;

@Table("imputacions")
public class Imputacio {

    ImputacioId id;

    String desc;

    Double hores;

    public Imputacio() {
        super();
    }

    public Imputacio(ImputacioId id, String desc, Double hores) {
        super();
        this.id = id;
        this.desc = desc;
        this.hores = hores;
    }

    public ImputacioId getId() {
        return id;
    }

    public void setId(ImputacioId id) {
        this.id = id;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public Double getHores() {
        return hores;
    }

    public void setHores(Double hores) {
        this.hores = hores;
    }

    @Override
    public String toString() {
        return "Imputacio [id=" + id + ", desc=" + desc + ", hores=" + hores + "]";
    }

}