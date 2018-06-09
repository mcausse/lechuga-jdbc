package linenetcom.ent;

import org.frijoles.annotated.anno.Generated;
import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;
import org.frijoles.mapper.autogen.HsqldbSequence;

@Table("projects")
public class Project {

    @Id
    @Generated(value = HsqldbSequence.class, args = "seq_projects")
    Long idProject;

    String name;

    public Project() {
        super();
    }

    public Project(Long idProject, String name) {
        super();
        this.idProject = idProject;
        this.name = name;
    }

    public Long getIdProject() {
        return idProject;
    }

    public void setIdProject(Long idProject) {
        this.idProject = idProject;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Project [idProject=" + idProject + ", name=" + name + "]";
    }

}
