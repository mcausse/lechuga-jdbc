package linenetcom;

import java.util.List;

import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.query.QueryBuilder;
import org.lechuga.jdbc.ScalarMappers;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.EntityManager;

import linenetcom.ent.Imputacio;
import linenetcom.ent.ImputacioId;
import linenetcom.ent.Project;
import linenetcom.ent.Tasca;
import linenetcom.ent.TascaId;
import linenetcom.ent.User;

public class LinenetService {

    final IEntityManagerFactory emf;

    final EntityManager<Project, Long> projectMan;
    final EntityManager<Tasca, TascaId> tascaMan;
    final EntityManager<User, Integer> userMan;
    final EntityManager<Imputacio, ImputacioId> imputacioMan;

    public LinenetService(IEntityManagerFactory emf) {
        super();
        this.emf = emf;
        this.projectMan = emf.buildEntityManager(Project.class);
        this.tascaMan = emf.buildEntityManager(Tasca.class);
        this.userMan = emf.buildEntityManager(User.class);
        this.imputacioMan = emf.buildEntityManager(Imputacio.class);
    }

    public List<Imputacio> getUserImputations(String email) {

        List<Imputacio> r;

        emf.getFacade().begin();
        try {

            QueryBuilder<Imputacio> q = emf.createQuery(Imputacio.class, "i");
            q.addEm("u", User.class);

            q.append("select {i.*} from {i} ");
            q.append("join {u} on {i.id.idUser}={u.idUser} ");
            q.append("where {u.email=?} ", email);
            q.append("order by {i.id.dia} desc ");

            r = q.getExecutor().load();

            emf.getFacade().commit();
        } catch (Exception e) {
            emf.getFacade().rollback();
            throw e;
        }
        return r;
    }

    public Double getHoresProjecte(long idProject, String nomTasca, Integer idUser) {

        double r;

        emf.getFacade().begin();
        try {

            QueryBuilder<Imputacio> q = emf.createQuery(Imputacio.class, "i");
            q.append("select sum({i.hores}) from {i} ");
            q.append("where {i.id.idProject=?} ", idProject);
            if (nomTasca != null) {
                q.append("and {i.id.nomTasca=?} ", nomTasca);
            }
            if (idUser != null) {
                q.append("and {i.id.idUser=?} ", idUser);
            }

            r = q.getExecutor().loadUnique(ScalarMappers.PDOUBLE);

            emf.getFacade().commit();
        } catch (Exception e) {
            emf.getFacade().rollback();
            throw e;
        }
        return r;
    }

    public void panic() {
        emf.getFacade().begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(emf.getFacade());
            sql.runFromClasspath("linenet.sql");
            emf.getFacade().commit();
        } catch (Exception e) {
            emf.getFacade().rollback();
            throw e;
        }

        emf.getFacade().begin();
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

            emf.getFacade().commit();
        } catch (Exception e) {
            emf.getFacade().rollback();
            throw e;
        }
    }
}
