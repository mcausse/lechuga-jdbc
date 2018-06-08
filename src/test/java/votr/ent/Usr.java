package votr.ent;

import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;

@Table("usrs")
public class Usr {

    @Id
    String hashUsr;

    String email;
    String alias;

    Integer numOpcioVotada;
    String hashVotacio;

    public Usr() {
        super();
    }

    public Usr(String hashUsr, String email, String alias, Integer numOpcioVotada, String hashVotacio) {
        super();
        this.hashUsr = hashUsr;
        this.email = email;
        this.alias = alias;
        this.numOpcioVotada = numOpcioVotada;
        this.hashVotacio = hashVotacio;
    }

    public String getHashUsr() {
        return hashUsr;
    }

    public void setHashUsr(String hashUsr) {
        this.hashUsr = hashUsr;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getAlias() {
        return alias;
    }

    public void setAlias(String alias) {
        this.alias = alias;
    }

    public Integer getNumOpcioVotada() {
        return numOpcioVotada;
    }

    public void setNumOpcioVotada(Integer numOpcioVotada) {
        this.numOpcioVotada = numOpcioVotada;
    }

    public String getHashVotacio() {
        return hashVotacio;
    }

    public void setHashVotacio(String hashVotacio) {
        this.hashVotacio = hashVotacio;
    }

    @Override
    public String toString() {
        return "Usr [hashUsr=" + hashUsr + ", email=" + email + ", alias=" + alias + ", numOpcioVotada="
                + numOpcioVotada + ", hashVotacio=" + hashVotacio + "]";
    }

}
