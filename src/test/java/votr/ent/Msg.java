package votr.ent;

import java.util.Date;

import org.frijoles.annotated.anno.Generated;
import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;
import org.frijoles.mapper.autogen.HsqldbSequence;

@Table("msgs")
public class Msg {

    @Id
    @Generated(value = HsqldbSequence.class, args = "seq_msg")
    Integer id;

    String text;

    Date data;
    String hashVotacio;

    public Msg() {
        super();
    }

    public Msg(Integer id, String text, Date data, String hashVotacio) {
        super();
        this.id = id;
        this.text = text;
        this.data = data;
        this.hashVotacio = hashVotacio;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public Date getData() {
        return data;
    }

    public void setData(Date data) {
        this.data = data;
    }

    public String getHashVotacio() {
        return hashVotacio;
    }

    public void setHashVotacio(String hashVotacio) {
        this.hashVotacio = hashVotacio;
    }

    @Override
    public String toString() {
        return "Msg [id=" + id + ", text=" + text + ", data=" + data + ", hashVotacio=" + hashVotacio + "]";
    }

}