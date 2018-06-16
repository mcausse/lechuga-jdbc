package movies.ent;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.GenericDao;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.HsqldbDDLGenerator;
import org.lechuga.mapper.autogen.HsqldbIdentity;
import org.lechuga.mapper.autogen.HsqldbSequence;

public class MoviesEnt {

    final DataAccesFacade facade;

    public MoviesEnt() {
        final JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:a");
        ds.setUser("sa");
        ds.setPassword("");
        this.facade = new JdbcDataAccesFacade(ds);
    }

    @Before
    public void before() {
        facade.begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(facade);
            sql.runFromClasspath("films.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testGen() throws Exception {

        String r = HsqldbDDLGenerator.generateScript(Film_.class, Actor_.class, FilmActor_.class);
        System.err.println(r);
    }

    @Test
    public void testName() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Film_.class, Actor_.class, FilmActor_.class);

        FilmsService serv = new FilmsService(emf);
        // TODO testarrrrrrrr
    }

    public static class FilmsService {

        final FilmsDao filmsDao;
        final FilmsActorDao filmsActorDao;
        final ActorDao actorDao;

        public FilmsService(IEntityManagerFactory emf) {
            super();
            this.filmsDao = new FilmsDao(emf);
            this.filmsActorDao = new FilmsActorDao(emf);
            this.actorDao = new ActorDao(emf);
        }

    }

    public static class FilmsDao extends GenericDao<Film, Long> {

        public FilmsDao(IEntityManagerFactory emf) {
            super(emf);
        }
    }

    public static class FilmsActorDao extends GenericDao<FilmActor, FilmActorId> {

        public FilmsActorDao(IEntityManagerFactory emf) {
            super(emf);
        }
    }

    public static class ActorDao extends GenericDao<Actor, Integer> {

        public ActorDao(IEntityManagerFactory emf) {
            super(emf);
        }
    }

    @Entity(entity = Film.class, table = "films")
    public static interface Film_ {

        @Generated(value = HsqldbSequence.class, args = "seq_film")
        @Id
        public static final MetaField<Film, Long> idFilm = new MetaField<>("id");
        public static final MetaField<Film, String> title = new MetaField<>("title");
        public static final MetaField<Film, Integer> year = new MetaField<>("year");
    }

    @Entity(entity = FilmActor.class, table = "film_actor")
    public static interface FilmActor_ {

        @Id
        public static final MetaField<FilmActor, Long> idFilm = new MetaField<>("id.idFilm");
        @Id
        public static final MetaField<FilmActor, Integer> idActor = new MetaField<>("id.idActor");
    }

    @Entity(entity = Actor.class, table = "actors")
    public static interface Actor_ {

        @Generated(value = HsqldbIdentity.class)
        @Id
        public static final MetaField<Actor, Integer> idActor = new MetaField<>("id");
        public static final MetaField<Actor, String> name = new MetaField<>("name");
    }

    public static class Film {

        Long id;
        String title;
        Integer year;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public Integer getYear() {
            return year;
        }

        public void setYear(Integer year) {
            this.year = year;
        }

    }

    public static class FilmActorId {

        Long idFilm;
        Integer idActor;

        public Long getIdFilm() {
            return idFilm;
        }

        public void setIdFilm(Long idFilm) {
            this.idFilm = idFilm;
        }

        public Integer getIdActor() {
            return idActor;
        }

        public void setIdActor(Integer idActor) {
            this.idActor = idActor;
        }

    }

    public static class FilmActor {

        FilmActorId id;

        public FilmActorId getId() {
            return id;
        }

        public void setId(FilmActorId id) {
            this.id = id;
        }

    }

    public static class Actor {

        Integer id;
        String name;

        public Integer getId() {
            return id;
        }

        public void setId(Integer id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

    }

}
