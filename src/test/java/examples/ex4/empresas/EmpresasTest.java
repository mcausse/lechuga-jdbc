package examples.ex4.empresas;

import java.util.ArrayList;
import java.util.List;

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
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.txproxy.TransactionalMethod;
import org.lechuga.jdbc.txproxy.TransactionalServiceProxyfier;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.jdbc.util.Triad;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.autogen.HsqldbSequence;

public class EmpresasTest {

    final DataAccesFacade facade;

    public EmpresasTest() {
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
            sql.runFromClasspath("empresas.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Empresa_.class, Direccion_.class,
                EmpresaDireccion_.class);

        Service service = TransactionalServiceProxyfier.proxyfy(facade, new ServiceImpl(emf), Service.class);

        Empresa aedea = new Empresa(null, "AEDEA");
        Empresa lifting = new Empresa(null, "LIFTING");

        Direccion serr = new Direccion(null, "SERR");
        Direccion ross = new Direccion(null, "ROSS");

        Direccion ilus = new Direccion(null, "ILUS");

        List<Triad<Empresa, ETipoDireccion, Direccion>> d = new ArrayList<>();
        d.add(new Triad<>(aedea, ETipoDireccion.FISCAL, serr));
        d.add(new Triad<>(aedea, ETipoDireccion.POSTAL, ross));
        d.add(new Triad<>(lifting, ETipoDireccion.POSTAL, ilus));

        service.store(d);
    }

    @Test
    public void testGen() throws Exception {
        String s = HsqldbDDLGenerator.generateScript(Empresa_.class, Direccion_.class, EmpresaDireccion_.class);
        System.err.println(s);
    }

    public static interface Service {

        @TransactionalMethod
        void store(List<Triad<Empresa, ETipoDireccion, Direccion>> d);

    }

    public static class ServiceImpl implements Service {

        final EntityManager<Empresa, Short> empresaEm;
        final EntityManager<EmpresaDireccion, EmpresaEmpleadoId> empresaEmpleadoEm;
        final EntityManager<Direccion, Long> empleadoEm;

        public ServiceImpl(IEntityManagerFactory emf) {
            super();
            this.empresaEm = emf.getEntityManager(Empresa.class);
            this.empresaEmpleadoEm = emf.getEntityManager(EmpresaDireccion.class);
            this.empleadoEm = emf.getEntityManager(Direccion.class);
        }

        @Override
        public void store(List<Triad<Empresa, ETipoDireccion, Direccion>> d) {
            d.forEach(p -> {
                Empresa empresa = p.getA();
                ETipoDireccion tipoDireccion = p.getB();
                Direccion direccion = p.getC();

                if (empresa.getIdEmpresa() == null) {
                    empresaEm.insert(empresa);
                }
                if (direccion.getIdDireccion() == null) {
                    empleadoEm.insert(direccion);
                }
                empresaEmpleadoEm.store(new EmpresaDireccion(
                        new EmpresaEmpleadoId(empresa.getIdEmpresa(), direccion.getIdDireccion()), tipoDireccion));
            });
        }

    }

    @Entity(entity = Empresa.class, table = "empresas")
    public static interface Empresa_ {

        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_empresas")
        public static final MetaField<Empresa, Long> idEmpresa = new MetaField<>("idEmpresa");
        public static final MetaField<Empresa, String> nombre = new MetaField<>("nombre");

        public static final OneToMany<Empresa, EmpresaDireccion> empresaEmpleados = new OneToMany<>(Empresa.class,
                EmpresaDireccion.class, new PropPair<>(Empresa_.idEmpresa, EmpresaDireccion_.idEmpresa));
    }

    @Entity(entity = EmpresaDireccion.class)
    public static interface EmpresaDireccion_ {

        @Id
        public static final MetaField<EmpresaDireccion, Long> idEmpresa = new MetaField<>("id.idEmpresa");
        @Id
        public static final MetaField<EmpresaDireccion, Integer> idDireccion = new MetaField<>("id.idDireccion");

        @EnumHandler
        public static final MetaField<EmpresaDireccion, ETipoDireccion> tipoDireccion = new MetaField<>(
                "tipoDireccion");

        public static final ManyToOne<EmpresaDireccion, Empresa> empresas = new ManyToOne<>(EmpresaDireccion.class,
                Empresa.class, new PropPair<>(EmpresaDireccion_.idEmpresa, Empresa_.idEmpresa));

        public static final ManyToOne<EmpresaDireccion, Direccion> direccion = new ManyToOne<>(EmpresaDireccion.class,
                Direccion.class, new PropPair<>(EmpresaDireccion_.idDireccion, Direccion_.idDireccion));

    }

    @Entity(entity = Direccion.class, table = "empleados")
    public static interface Direccion_ {

        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_empleados")
        public static final MetaField<Direccion, Integer> idDireccion = new MetaField<>("idDireccion");
        public static final MetaField<Direccion, String> nombre = new MetaField<>("nombre");

        public static final OneToMany<Direccion, EmpresaDireccion> empresaEmpleados = new OneToMany<>(Direccion.class,
                EmpresaDireccion.class, new PropPair<>(Direccion_.idDireccion, EmpresaDireccion_.idDireccion));

    }

    public static class Empresa {
        Long idEmpresa;
        String nombre;

        public Empresa() {
            super();
        }

        public Empresa(Long idEmpresa, String nombre) {
            super();
            this.idEmpresa = idEmpresa;
            this.nombre = nombre;
        }

        public Long getIdEmpresa() {
            return idEmpresa;
        }

        public void setIdEmpresa(Long idEmpresa) {
            this.idEmpresa = idEmpresa;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return "Empresa [idEmpresa=" + idEmpresa + ", nombre=" + nombre + "]";
        }

    }

    public static class EmpresaEmpleadoId {
        Long idEmpresa;
        Integer idDireccion;

        public EmpresaEmpleadoId() {
            super();
        }

        public EmpresaEmpleadoId(Long idEmpresa, Integer idDireccion) {
            super();
            this.idEmpresa = idEmpresa;
            this.idDireccion = idDireccion;
        }

        public Long getIdEmpresa() {
            return idEmpresa;
        }

        public void setIdEmpresa(Long idEmpresa) {
            this.idEmpresa = idEmpresa;
        }

        public Integer getIdDireccion() {
            return idDireccion;
        }

        public void setIdDireccion(Integer idDireccion) {
            this.idDireccion = idDireccion;
        }

        @Override
        public String toString() {
            return "EmpresaEmpleadoId [idEmpresa=" + idEmpresa + ", idDireccion=" + idDireccion + "]";
        }

    }

    public static class EmpresaDireccion {
        EmpresaEmpleadoId id;
        ETipoDireccion tipoDireccion;

        public EmpresaDireccion() {
            super();
        }

        public EmpresaDireccion(EmpresaEmpleadoId id, ETipoDireccion tipoDireccion) {
            super();
            this.id = id;
            this.tipoDireccion = tipoDireccion;
        }

        public EmpresaEmpleadoId getId() {
            return id;
        }

        public void setId(EmpresaEmpleadoId id) {
            this.id = id;
        }

        public ETipoDireccion getTipoDireccion() {
            return tipoDireccion;
        }

        public void setTipoDireccion(ETipoDireccion tipoDireccion) {
            this.tipoDireccion = tipoDireccion;
        }

        @Override
        public String toString() {
            return "EmpresaDireccion [id=" + id + ", tipoDireccion=" + tipoDireccion + "]";
        }

    }

    public static class Direccion {
        Integer idDireccion;
        String nombre;

        public Direccion() {
            super();
        }

        public Direccion(Integer idDireccion, String nombre) {
            super();
            this.idDireccion = idDireccion;
            this.nombre = nombre;
        }

        public Integer getIdDireccion() {
            return idDireccion;
        }

        public void setIdDireccion(Integer idDireccion) {
            this.idDireccion = idDireccion;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return "Direccion [idDireccion=" + idDireccion + ", nombre=" + nombre + "]";
        }

    }

    public static enum ETipoDireccion {
        POSTAL, FISCAL;
    }

}
