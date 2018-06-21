package examples.ex3.votr;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.annotated.HsqldbDDLGenerator;
import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.ManyToOne;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.OneToMany;
import org.lechuga.annotated.PropPair;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.txproxy.TransactionalMethod;
import org.lechuga.jdbc.txproxy.TransactionalServiceProxyfier;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.GenericDao;
import org.lechuga.mapper.Order;
import org.lechuga.mapper.autogen.HsqldbIdentity;

import examples.ex3.votr.VotrTest.VotrService.VotacioDto;

public class VotrTest {

    final DataAccesFacade facade;

    public VotrTest() {
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
            sql.runFromClasspath("votr.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testGen() throws Exception {

        String r = HsqldbDDLGenerator.generateScript(Usuari_.class, Opcio_.class, Votacio_.class);
        System.err.println(r);
    }

    @Test
    public void testName() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Usuari_.class, Opcio_.class, Votacio_.class);

        VotrService service = TransactionalServiceProxyfier.proxyfy(facade, new VotrServiceImpl(emf),
                VotrService.class);

        Votacio v = new Votacio("aaa", "bestseller", "lo-bestseller", new Date(0L), null);
        Opcio o1 = new Opcio(null, "eneida", "la-eneida");
        Opcio o2 = new Opcio(null, "odissea", "la-odissea");
        Usuari mhc = new Usuari(new UsuariId("bbb", null), "mhc@votr.com", null, null, null);
        Usuari msm = new Usuari(new UsuariId("ccc", null), "msm@votr.com", null, null, null);

        service.create(v, Arrays.asList(o1, o2), Arrays.asList(mhc, msm));

        service.usuariModificaAlias(v.getHashVotacio(), mhc.getId().getHashUsuari(), "mhoms");
        service.usuariVota(v.getHashVotacio(), mhc.getId().getHashUsuari(), o1.getId().getIdOpcio());

        VotacioDto dto = service.loadState(mhc.getId().getHashVotacio(), mhc.getId().getHashUsuari());

        assertEquals(
                "VotacioDto [votacio=Votacio [hashVotacio=aaa, nom=bestseller, descripcio=lo-bestseller, dataInici=1970-01-01 01:00:00.0, dataFi=null], "
                        + "usuari=Usuari [id=UsuariId [hashUsuari=bbb, hashVotacio=aaa], email=mhc@votr.com, alias=mhoms, idOpcioVotada=100, dataVotacio=1970-01-01 01:00:00.0], "
                        + "usuariOpcioVotada=Opcio [id=OpcioId [idOpcio=100, hashVotacio=aaa], nom=eneida, descripcio=la-eneida], "
                        + "opcionsUsuaris="
                        + "{Opcio [id=OpcioId [idOpcio=100, hashVotacio=aaa], nom=eneida, descripcio=la-eneida]=[Usuari [id=UsuariId [hashUsuari=bbb, hashVotacio=aaa], email=mhc@votr.com, alias=mhoms, idOpcioVotada=100, dataVotacio=1970-01-01 01:00:00.0]], "
                        + "Opcio [id=OpcioId [idOpcio=101, hashVotacio=aaa], nom=odissea, descripcio=la-odissea]=[]}]",
                dto.toString());

    }

    public static interface VotrService {

        @TransactionalMethod
        void create(Votacio votacio, List<Opcio> opcions, List<Usuari> usuaris);

        @TransactionalMethod
        void usuariModificaAlias(String hashVotacio, String hashUsuari, String alias);

        @TransactionalMethod
        void usuariVota(String hashVotacio, String hashUsuari, Long idOpcioVotada);

        @TransactionalMethod(readOnly = true)
        VotacioDto loadState(String hashVotacio, String hashUsuari);

        public static class VotacioDto {

            public final Votacio votacio;
            public final Usuari usuari;
            public final Opcio usuariOpcioVotada;
            public final Map<Opcio, List<Usuari>> opcionsUsuaris;

            public VotacioDto(Votacio votacio, Usuari usuari, Opcio usuariOpcioVotada,
                    Map<Opcio, List<Usuari>> opcionsUsuaris) {
                super();
                this.votacio = votacio;
                this.usuari = usuari;
                this.usuariOpcioVotada = usuariOpcioVotada;
                this.opcionsUsuaris = opcionsUsuaris;
            }

            @Override
            public String toString() {
                return "VotacioDto [votacio=" + votacio + ", usuari=" + usuari + ", usuariOpcioVotada="
                        + usuariOpcioVotada + ", opcionsUsuaris=" + opcionsUsuaris + "]";
            }

        }
    }

    public static class VotacionsDao extends GenericDao<Votacio, String> {

        public VotacionsDao(IEntityManagerFactory emf) {
            super(emf);
        }
    }

    public static class OpcionsDao extends GenericDao<Opcio, OpcioId> {

        public OpcionsDao(IEntityManagerFactory emf) {
            super(emf);
        }
    }

    public static class UsuarisDao extends GenericDao<Usuari, UsuariId> {

        public UsuarisDao(IEntityManagerFactory emf) {
            super(emf);
        }
    }

    public static class VotrServiceImpl implements VotrService {

        final IEntityManagerFactory emf;
        final VotacionsDao votacionsDao;
        final OpcionsDao opcionsDao;
        final UsuarisDao usuarisDao;

        public VotrServiceImpl(IEntityManagerFactory emf) {
            super();
            this.emf = emf;
            this.votacionsDao = new VotacionsDao(emf);
            this.opcionsDao = new OpcionsDao(emf);
            this.usuarisDao = new UsuarisDao(emf);
        }

        @Override
        public void create(Votacio votacio, List<Opcio> opcions, List<Usuari> usuaris) {

            votacio.setDataInici(new Date(0L));
            votacionsDao.store(votacio);

            opcions.forEach(o -> {
                o.setId(new OpcioId());
                o.getId().setHashVotacio(votacio.getHashVotacio());
                opcionsDao.store(o);
            });

            usuaris.forEach(u -> {
                u.getId().setHashVotacio(votacio.getHashVotacio());
                u.setAlias(null);
                u.setIdOpcioVotada(null);
                u.setDataVotacio(null);
                usuarisDao.store(u);
            });
        }

        @SuppressWarnings("unchecked")
        @Override
        public void usuariModificaAlias(String hashVotacio, String hashUsuari, String alias) {

            Usuari usuari = usuarisDao.loadById(new UsuariId(hashUsuari, hashVotacio));
            usuari.setAlias(alias);
            usuarisDao.update(usuari, Usuari_.alias);
        }

        @SuppressWarnings("unchecked")
        @Override
        public void usuariVota(String hashVotacio, String hashUsuari, Long idOpcioVotada) {

            Usuari usuari = usuarisDao.loadById(new UsuariId(hashUsuari, hashVotacio));
            usuari.setIdOpcioVotada(idOpcioVotada);
            usuari.setDataVotacio(new Date(0L));
            usuarisDao.update(usuari, Usuari_.idOpcioVotada, Usuari_.dataVotacio);
        }

        @Override
        public VotacioDto loadState(String hashVotacio, String hashUsuari) {

            Usuari usuari = usuarisDao.loadById(new UsuariId(hashUsuari, hashVotacio));
            Votacio votacio = Usuari_.votacio.load(emf, usuari);
            Opcio opcioVotada = Usuari_.opcioVotada.load(emf, usuari);

            Map<Opcio, List<Usuari>> opcionsUsuaris = new LinkedHashMap<>();
            List<Opcio> opcions = Votacio_.opcions.load(emf, votacio, Order.asc(Opcio_.idOpcio));
            opcions.forEach(o -> {
                List<Usuari> usuarisVotats = Opcio_.usuarisVotats.load(emf, o, Order.asc(Usuari_.email));
                opcionsUsuaris.put(o, usuarisVotats);
            });

            return new VotacioDto(votacio, usuari, opcioVotada, opcionsUsuaris);
        }

    }

    @Entity(entity = Votacio.class, table = "votacions")
    public static class Votacio_ {

        @Id
        public static final MetaField<Votacio, String> hashVotacio = new MetaField<>("hashVotacio");
        public static final MetaField<Votacio, String> nom = new MetaField<>("nom");
        public static final MetaField<Votacio, String> descripcio = new MetaField<>("descripcio");
        public static final MetaField<Votacio, Date> dataInici = new MetaField<>("dataInici");
        public static final MetaField<Votacio, Date> dataFi = new MetaField<>("dataFi");

        public static final OneToMany<Votacio, Opcio> opcions = new OneToMany<>( //
                Votacio.class, Opcio.class, //
                new PropPair<>(Votacio_.hashVotacio, Opcio_.hashVotacio) //
        );
        public static final OneToMany<Votacio, Usuari> usuaris = new OneToMany<>( //
                Votacio.class, Usuari.class, //
                new PropPair<>(Votacio_.hashVotacio, Usuari_.hashVotacio) //
        );
    }

    public static class Votacio {

        String hashVotacio;

        String nom;
        String descripcio;
        Date dataInici;
        Date dataFi;

        public Votacio() {
            super();
        }

        public Votacio(String hashVotacio, String nom, String descripcio, Date dataInici, Date dataFi) {
            super();
            this.hashVotacio = hashVotacio;
            this.nom = nom;
            this.descripcio = descripcio;
            this.dataInici = dataInici;
            this.dataFi = dataFi;
        }

        public String getHashVotacio() {
            return hashVotacio;
        }

        public void setHashVotacio(String hashVotacio) {
            this.hashVotacio = hashVotacio;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getDescripcio() {
            return descripcio;
        }

        public void setDescripcio(String descripcio) {
            this.descripcio = descripcio;
        }

        public Date getDataInici() {
            return dataInici;
        }

        public void setDataInici(Date dataInici) {
            this.dataInici = dataInici;
        }

        public Date getDataFi() {
            return dataFi;
        }

        public void setDataFi(Date dataFi) {
            this.dataFi = dataFi;
        }

        @Override
        public String toString() {
            return "Votacio [hashVotacio=" + hashVotacio + ", nom=" + nom + ", descripcio=" + descripcio
                    + ", dataInici=" + dataInici + ", dataFi=" + dataFi + "]";
        }

    }

    @Entity(entity = Opcio.class, table = "opcions")
    public static class Opcio_ {

        @Id
        @Generated(value = HsqldbIdentity.class)
        public static final MetaField<Opcio, Long> idOpcio = new MetaField<>("id.idOpcio");
        @Id
        public static final MetaField<Opcio, String> hashVotacio = new MetaField<>("id.hashVotacio");

        public static final MetaField<Opcio, String> nom = new MetaField<>("nom");
        public static final MetaField<Opcio, String> descripcio = new MetaField<>("descripcio");

        public static final OneToMany<Opcio, Usuari> usuarisVotats = new OneToMany<>( //
                Opcio.class, Usuari.class, //
                new PropPair<>(Opcio_.idOpcio, Usuari_.idOpcioVotada) //
        );
    }

    public static class OpcioId {

        Long idOpcio;
        String hashVotacio;

        public OpcioId() {
            super();
        }

        public OpcioId(Long idOpcio, String hashVotacio) {
            super();
            this.idOpcio = idOpcio;
            this.hashVotacio = hashVotacio;
        }

        public Long getIdOpcio() {
            return idOpcio;
        }

        public void setIdOpcio(Long idOpcio) {
            this.idOpcio = idOpcio;
        }

        public String getHashVotacio() {
            return hashVotacio;
        }

        public void setHashVotacio(String hashVotacio) {
            this.hashVotacio = hashVotacio;
        }

        @Override
        public String toString() {
            return "OpcioId [idOpcio=" + idOpcio + ", hashVotacio=" + hashVotacio + "]";
        }

    }

    public static class Opcio {

        OpcioId id;

        String nom;
        String descripcio;

        public Opcio() {
            super();
        }

        public Opcio(OpcioId id, String nom, String descripcio) {
            super();
            this.id = id;
            this.nom = nom;
            this.descripcio = descripcio;
        }

        public OpcioId getId() {
            return id;
        }

        public void setId(OpcioId id) {
            this.id = id;
        }

        public String getNom() {
            return nom;
        }

        public void setNom(String nom) {
            this.nom = nom;
        }

        public String getDescripcio() {
            return descripcio;
        }

        public void setDescripcio(String descripcio) {
            this.descripcio = descripcio;
        }

        @Override
        public String toString() {
            return "Opcio [id=" + id + ", nom=" + nom + ", descripcio=" + descripcio + "]";
        }

    }

    @Entity(entity = Usuari.class, table = "usuaris")
    public static class Usuari_ {

        @Id
        public static final MetaField<Usuari, String> hashUsuari = new MetaField<>("id.hashUsuari");
        @Id
        public static final MetaField<Usuari, String> hashVotacio = new MetaField<>("id.hashVotacio");

        public static final MetaField<Usuari, String> email = new MetaField<>("email");
        public static final MetaField<Usuari, String> alias = new MetaField<>("alias");

        public static final MetaField<Usuari, String> idOpcioVotada = new MetaField<>("idOpcioVotada");
        public static final MetaField<Usuari, Date> dataVotacio = new MetaField<>("dataVotacio");

        public static final ManyToOne<Usuari, Votacio> votacio = new ManyToOne<>( //
                Usuari.class, Votacio.class, //
                new PropPair<>(Usuari_.hashVotacio, Votacio_.hashVotacio) //
        );
        public static final ManyToOne<Usuari, Opcio> opcioVotada = new ManyToOne<>( //
                Usuari.class, Opcio.class, //
                new PropPair<>(Usuari_.hashVotacio, Opcio_.hashVotacio), //
                new PropPair<>(Usuari_.idOpcioVotada, Opcio_.idOpcio) //
        );
    }

    public static class UsuariId {

        String hashUsuari;
        String hashVotacio;

        public UsuariId() {
            super();
        }

        public UsuariId(String hashUsuari, String hashVotacio) {
            super();
            this.hashUsuari = hashUsuari;
            this.hashVotacio = hashVotacio;
        }

        public String getHashUsuari() {
            return hashUsuari;
        }

        public void setHashUsuari(String hashUsuari) {
            this.hashUsuari = hashUsuari;
        }

        public String getHashVotacio() {
            return hashVotacio;
        }

        public void setHashVotacio(String hashVotacio) {
            this.hashVotacio = hashVotacio;
        }

        @Override
        public String toString() {
            return "UsuariId [hashUsuari=" + hashUsuari + ", hashVotacio=" + hashVotacio + "]";
        }

    }

    public static class Usuari {

        UsuariId id;

        String email;
        String alias;

        Long idOpcioVotada;
        Date dataVotacio;

        public Usuari() {
            super();
        }

        public Usuari(UsuariId id, String email, String alias, Long idOpcioVotada, Date dataVotacio) {
            super();
            this.id = id;
            this.email = email;
            this.alias = alias;
            this.idOpcioVotada = idOpcioVotada;
            this.dataVotacio = dataVotacio;
        }

        public UsuariId getId() {
            return id;
        }

        public void setId(UsuariId id) {
            this.id = id;
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

        public Long getIdOpcioVotada() {
            return idOpcioVotada;
        }

        public void setIdOpcioVotada(Long idOpcioVotada) {
            this.idOpcioVotada = idOpcioVotada;
        }

        public Date getDataVotacio() {
            return dataVotacio;
        }

        public void setDataVotacio(Date dataVotacio) {
            this.dataVotacio = dataVotacio;
        }

        @Override
        public String toString() {
            return "Usuari [id=" + id + ", email=" + email + ", alias=" + alias + ", idOpcioVotada=" + idOpcioVotada
                    + ", dataVotacio=" + dataVotacio + "]";
        }

    }

}
