package linenetcom.ent;

import org.frijoles.annotated.anno.CustomHandler;
import org.frijoles.annotated.anno.Id;
import org.frijoles.mapper.handler.custom.StringDateHandler;

public class ImputacioId {

    @Id
    Long idProject;

    @Id
    Integer idUser;

    @Id
    String nomTasca;

    @Id
    @CustomHandler(value = StringDateHandler.class, args = "dd/MM/yyyy")
    String dia;

    public ImputacioId() {
        super();
    }

    public ImputacioId(Long idProject, Integer idUser, String nomTasca, String dia) {
        super();
        this.idProject = idProject;
        this.idUser = idUser;
        this.nomTasca = nomTasca;
        this.dia = dia;
    }

    public Long getIdProject() {
        return idProject;
    }

    public void setIdProject(Long idProject) {
        this.idProject = idProject;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getNomTasca() {
        return nomTasca;
    }

    public void setNomTasca(String nomTasca) {
        this.nomTasca = nomTasca;
    }

    public String getDia() {
        return dia;
    }

    public void setDia(String dia) {
        this.dia = dia;
    }

    @Override
    public String toString() {
        return "ImputacioId [idProject=" + idProject + ", idUser=" + idUser + ", dia=" + dia + "]";
    }

}
