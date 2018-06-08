package votr.ent;

import java.util.Date;

import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;

@Table("votacions")
public class Votacio {

    @Id
    String hashVotacio;

    String titol;
    String descripcio;

    Date dataCreacio;
    Date dataFi;

    public Votacio() {
        super();
    }

    public Votacio(String hashVotacio, String titol, String descripcio, Date dataCreacio, Date dataFi) {
        super();
        this.hashVotacio = hashVotacio;
        this.titol = titol;
        this.descripcio = descripcio;
        this.dataCreacio = dataCreacio;
        this.dataFi = dataFi;
    }

    public String getHashVotacio() {
        return hashVotacio;
    }

    public void setHashVotacio(String hashVotacio) {
        this.hashVotacio = hashVotacio;
    }

    public String getDescripcio() {
        return descripcio;
    }

    public void setDescripcio(String descripcio) {
        this.descripcio = descripcio;
    }

    public Date getDataCreacio() {
        return dataCreacio;
    }

    public void setDataCreacio(Date dataCreacio) {
        this.dataCreacio = dataCreacio;
    }

    public Date getDataFi() {
        return dataFi;
    }

    public void setDataFi(Date dataFi) {
        this.dataFi = dataFi;
    }

    public String getTitol() {
        return titol;
    }

    public void setTitol(String titol) {
        this.titol = titol;
    }

    @Override
    public String toString() {
        return "Votacio [hashVotacio=" + hashVotacio + ", titol=" + titol + ", descripcio=" + descripcio
                + ", dataCreacio=" + dataCreacio + ", dataFi=" + dataFi + "]";
    }

}
