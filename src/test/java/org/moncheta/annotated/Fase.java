package org.moncheta.annotated;

public class Fase {

    int idFase;
    String codiFase;

    public Fase() {
        super();
    }

    public Fase(int idFase, String codiFase) {
        super();
        this.idFase = idFase;
        this.codiFase = codiFase;
    }

    public int getIdFase() {
        return idFase;
    }

    public void setIdFase(int idFase) {
        this.idFase = idFase;
    }

    public String getCodiFase() {
        return codiFase;
    }

    public void setCodiFase(String codiFase) {
        this.codiFase = codiFase;
    }

    @Override
    public String toString() {
        return "Fase [idFase=" + idFase + ", codiFase=" + codiFase + "]";
    }

}