package linenetcom.ent;

import org.frijoles.annotated.anno.Id;

public class TascaId {
    @Id
    Long idProject;
    @Id
    String nomTasca;

    public TascaId() {
        super();
    }

    public TascaId(Long idProject, String nomTasca) {
        super();
        this.idProject = idProject;
        this.nomTasca = nomTasca;
    }

    public Long getIdProject() {
        return idProject;
    }

    public void setIdProject(Long idProject) {
        this.idProject = idProject;
    }

    public String getNomTasca() {
        return nomTasca;
    }

    public void setNomTasca(String nomTasca) {
        this.nomTasca = nomTasca;
    }

    @Override
    public String toString() {
        return "TascaId [idProject=" + idProject + ", nomTasca=" + nomTasca + "]";
    }

}
