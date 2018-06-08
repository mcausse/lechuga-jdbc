package votr.ent;

import org.frijoles.annotated.anno.Id;

public class OpcioId {

    @Id
    String hashVotacio;
    @Id
    Integer num;

    public OpcioId() {
        super();
    }

    public OpcioId(String hashVotacio, Integer num) {
        super();
        this.hashVotacio = hashVotacio;
        this.num = num;
    }

    public String getHashVotacio() {
        return hashVotacio;
    }

    public void setHashVotacio(String hashVotacio) {
        this.hashVotacio = hashVotacio;
    }

    public Integer getNum() {
        return num;
    }

    public void setNum(Integer num) {
        this.num = num;
    }

    @Override
    public String toString() {
        return "OpcioId [hashVotacio=" + hashVotacio + ", num=" + num + "]";
    }

}
