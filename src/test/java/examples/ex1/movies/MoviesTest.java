package examples.ex1.movies;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.annotated.HsqldbDDLGenerator;
import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.ManyToMany;
import org.lechuga.annotated.ManyToOne;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.OneToMany;
import org.lechuga.annotated.PropPair;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.extractor.MapResultSetExtractor;
import org.lechuga.jdbc.util.Pair;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.GenericDao;
import org.lechuga.mapper.autogen.HsqldbIdentity;
import org.lechuga.mapper.autogen.HsqldbSequence;

public class MoviesTest {

    final DataAccesFacade facade;

    public MoviesTest() {
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

        Film closeRange = new Film("At Close Range", 1986);
        Actor cwalken = new Actor("CWALKEN");
        Actor spenn = new Actor("SPENN");

        Film deadZone = new Film("The Dead Zone", 1983);

        Film warDogs = new Film("War Dogs", 1983);

        Film reservoirDogs = new Film("Reservoir Dogs", 1992);
        Actor cpenn = new Actor("CPENN");

        serv.create(closeRange, Arrays.asList(cwalken, spenn, cpenn));
        serv.create(warDogs, Arrays.asList(cwalken));
        serv.create(deadZone, Arrays.asList(cwalken));
        serv.create(reservoirDogs, Arrays.asList(cpenn));

        List<Map<String, Object>> r = serv.getActorFitness();
        assertEquals(
                "[{ID=100, NAME=CWALKEN, NUM_FILMS=3}, {ID=102, NAME=CPENN, NUM_FILMS=2}, {ID=101, NAME=SPENN, NUM_FILMS=1}]",
                r.toString());

        facade.begin();
        try {

            List<FilmActor> closeRangeFilmActors = Film_.filmActors.load(emf, closeRange);

            assertEquals("[FilmActor [id=FilmActorId [idFilm=10, idActor=100]], "
                    + "FilmActor [id=FilmActorId [idFilm=10, idActor=101]], "
                    + "FilmActor [id=FilmActorId [idFilm=10, idActor=102]]]", closeRangeFilmActors.toString());

            List<Pair<FilmActor, Actor>> actorsAtCloseRange = FilmActor_.actors.load(emf, closeRangeFilmActors);

            assertEquals(
                    "[Pair [left=FilmActor [id=FilmActorId [idFilm=10, idActor=100]], right=Actor [id=100, name=CWALKEN]], "
                            + "Pair [left=FilmActor [id=FilmActorId [idFilm=10, idActor=101]], right=Actor [id=101, name=SPENN]], "
                            + "Pair [left=FilmActor [id=FilmActorId [idFilm=10, idActor=102]], right=Actor [id=102, name=CPENN]]]",
                    actorsAtCloseRange.toString());

            List<Actor> actorsAtCloseRange2 = Film_.actors.load(emf, closeRange);
            assertEquals("[Actor [id=100, name=CWALKEN], Actor [id=101, name=SPENN], Actor [id=102, name=CPENN]]",
                    actorsAtCloseRange2.toString());

            List<Actor> actorsAtWarDogs = Film_.actors.load(emf, warDogs);
            assertEquals("[Actor [id=100, name=CWALKEN]]", actorsAtWarDogs.toString());

        } finally {
            facade.rollback();
        }

    }

    public static class FilmsService {

        final IEntityManagerFactory emf;
        final FilmsDao filmsDao;
        final FilmsActorDao filmsActorDao;
        final ActorDao actorDao;

        public FilmsService(IEntityManagerFactory emf) {
            super();
            this.emf = emf;
            this.filmsDao = new FilmsDao(emf);
            this.filmsActorDao = new FilmsActorDao(emf);
            this.actorDao = new ActorDao(emf);
        }

        public void create(Film film, List<Actor> actors) {

            emf.getFacade().begin();
            try {

                filmsDao.store(film);
                actorDao.store(actors);

                List<FilmActor> fas = new ArrayList<>();
                actors.forEach(a -> fas.add(new FilmActor(new FilmActorId(film.getId(), a.getId()))));
                filmsActorDao.store(fas);

                emf.getFacade().commit();
            } catch (Exception e) {
                emf.getFacade().rollback();
                throw new RuntimeException(e);
            }
        }

        public List<Map<String, Object>> getActorFitness() {

            emf.getFacade().begin();
            try {

                CriteriaBuilder c = emf.createCriteria();
                // Restrictions<Film> rf = emf.getRestrictions(Film.class, "f");
                Restrictions<FilmActor> rfa = emf.getRestrictions(FilmActor.class, "fa");
                Restrictions<Actor> ra = emf.getRestrictions(Actor.class, "a");

                c.append("select {}, count(*) as num_films ", ra.all());
                c.append("from {} ", ra.table());
                c.append("join {} on {} ", rfa.table(), rfa.eq(FilmActor_.idActor, ra, Actor_.idActor));
                c.append("group by {} ", ra.all());
                c.append("order by num_films desc ");

                List<Map<String, Object>> r = c.extract(new MapResultSetExtractor());
                return r;

            } finally {
                emf.getFacade().rollback();
            }
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

        public static final OneToMany<Film, FilmActor> filmActors = new OneToMany<>( //
                Film.class, FilmActor.class, //
                new PropPair<>(Film_.idFilm, FilmActor_.idFilm));

        // XXX ManyToMany !!!
        public static final ManyToMany<Film, FilmActor, Actor> actors = new ManyToMany<>(Film_.filmActors,
                FilmActor_.actors);

    }

    @Entity(entity = FilmActor.class, table = "film_actor")
    public static interface FilmActor_ {

        @Id
        public static final MetaField<FilmActor, Long> idFilm = new MetaField<>("id.idFilm");
        @Id
        public static final MetaField<FilmActor, Integer> idActor = new MetaField<>("id.idActor");

        public static final ManyToOne<FilmActor, Film> films = new ManyToOne<>( //
                FilmActor.class, Film.class, //
                new PropPair<>(FilmActor_.idFilm, Film_.idFilm));

        public static final ManyToOne<FilmActor, Actor> actors = new ManyToOne<>( //
                FilmActor.class, Actor.class, //
                new PropPair<>(FilmActor_.idActor, Actor_.idActor));
    }

    @Entity(entity = Actor.class, table = "actors")
    public static interface Actor_ {

        @Generated(value = HsqldbIdentity.class)
        @Id
        public static final MetaField<Actor, Integer> idActor = new MetaField<>("id");
        public static final MetaField<Actor, String> name = new MetaField<>("name");

        public static final OneToMany<Actor, FilmActor> filmActors = new OneToMany<>( //
                Actor.class, FilmActor.class, //
                new PropPair<>(Actor_.idActor, FilmActor_.idActor));

    }

    public static class Film {

        Long id;
        String title;
        Integer year;

        public Film() {
            super();
        }

        public Film(String title, Integer year) {
            super();
            this.title = title;
            this.year = year;
        }

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

        @Override
        public String toString() {
            return "Film [id=" + id + ", title=" + title + ", year=" + year + "]";
        }

    }

    public static class FilmActorId {

        Long idFilm;
        Integer idActor;

        public FilmActorId() {
            super();
        }

        public FilmActorId(Long idFilm, Integer idActor) {
            super();
            this.idFilm = idFilm;
            this.idActor = idActor;
        }

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

        @Override
        public String toString() {
            return "FilmActorId [idFilm=" + idFilm + ", idActor=" + idActor + "]";
        }

    }

    public static class FilmActor {

        FilmActorId id;

        public FilmActor() {
            super();
        }

        public FilmActor(FilmActorId id) {
            super();
            this.id = id;
        }

        public FilmActorId getId() {
            return id;
        }

        public void setId(FilmActorId id) {
            this.id = id;
        }

        @Override
        public String toString() {
            return "FilmActor [id=" + id + "]";
        }

    }

    public static class Actor {

        Integer id;
        String name;

        public Actor() {
            super();
        }

        public Actor(String name) {
            super();
            this.name = name;
        }

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

        @Override
        public String toString() {
            return "Actor [id=" + id + ", name=" + name + "]";
        }

    }

}
