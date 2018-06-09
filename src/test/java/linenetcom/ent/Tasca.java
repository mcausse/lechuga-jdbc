package linenetcom.ent;

import org.lechuga.annotated.anno.Table;

@Table("tasques")
public class Tasca {

    TascaId id;

    public Tasca() {
        super();
    }

    public Tasca(TascaId id) {
        super();
        this.id = id;
    }

    public TascaId getId() {
        return id;
    }

    public void setId(TascaId id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "Tasca [id=" + id + "]";
    }

}
