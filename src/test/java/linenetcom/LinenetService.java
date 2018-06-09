package linenetcom;

import java.util.List;

import org.frijoles.annotated.EntityManagerFactory;
import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.ScalarMappers;
import org.frijoles.jdbc.util.SqlScriptExecutor;
import org.frijoles.mapper.EntityManager;
import org.frijoles.mapper.query.QueryBuilder;

import linenetcom.ent.Imputacio;
import linenetcom.ent.ImputacioId;
import linenetcom.ent.Project;
import linenetcom.ent.Tasca;
import linenetcom.ent.TascaId;
import linenetcom.ent.User;

public class LinenetService {

    final DataAccesFacade facade;

    final EntityManager<Project, Long> projectMan;
    final EntityManager<Tasca, TascaId> tascaMan;
    final EntityManager<User, Integer> userMan;
    final EntityManager<Imputacio, ImputacioId> imputacioMan;

    public LinenetService(DataAccesFacade facade) {
        super();
        this.facade = facade;

        EntityManagerFactory emf = new EntityManagerFactory(facade);
        this.projectMan = emf.build(Project.class, Long.class);
        this.tascaMan = emf.build(Tasca.class, TascaId.class);
        this.userMan = emf.build(User.class, Integer.class);
        this.imputacioMan = emf.build(Imputacio.class, ImputacioId.class);
    }

    public List<Imputacio> getUserImputations(String email) {

        List<Imputacio> r;

        facade.begin();
        try {

            QueryBuilder<Imputacio> q = imputacioMan.createQuery("i");
            q.addEm("u", userMan);

            q.append("select {i.*} from {i} ");
            q.append("join {u} on {i.id.idUser}={u.idUser} ");
            q.append("where {u.email=?} ", email);
            q.append("order by {i.id.dia} desc ");

            r = q.getExecutor().load();

            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
        return r;
    }

    public Double getHoresProjecte(long idProject, String nomTasca, Integer idUser) {

        double r;

        facade.begin();
        try {

            QueryBuilder<Imputacio> q = imputacioMan.createQuery("i");
            q.append("select sum({i.hores}) from {i} ");
            q.append("where {i.id.idProject=?} ", idProject);
            if (nomTasca != null) {
                q.append("and {i.id.nomTasca=?} ", nomTasca);
            }
            if (idUser != null) {
                q.append("and {i.id.idUser=?} ", idUser);
            }

            r = q.getExecutor().loadUnique(ScalarMappers.PDOUBLE);

            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
        return r;
    }

    public void panic() {
        facade.begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(facade);
            sql.runFromClasspath("linenet.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }

        facade.begin();
        try {

            Project ringo = new Project(null, "Ringo");
            projectMan.store(ringo);

            Tasca fase1 = new Tasca(new TascaId(ringo.getIdProject(), "FASE1"));
            Tasca fase2 = new Tasca(new TascaId(ringo.getIdProject(), "FASE2"));
            tascaMan.store(fase1);
            tascaMan.store(fase2);

            User mmf = new User(null, "mmf@linenet.com");
            User eib = new User(null, "eib@linenet.com");
            User mhc = new User(null, "mhc@linenet.com");
            userMan.store(mmf);
            userMan.store(eib);
            userMan.store(mhc);

            imputacioMan.store(new Imputacio(
                    new ImputacioId(ringo.getIdProject(), mhc.getIdUser(), "FASE1", "01/01/2014"), "aaa", 3.5));
            imputacioMan.store(new Imputacio(
                    new ImputacioId(ringo.getIdProject(), mhc.getIdUser(), "FASE1", "03/01/2014"), "bbb", 4.5));
            imputacioMan.store(new Imputacio(
                    new ImputacioId(ringo.getIdProject(), eib.getIdUser(), "FASE1", "01/01/2014"), "ccc", 5.5));

            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }
}
