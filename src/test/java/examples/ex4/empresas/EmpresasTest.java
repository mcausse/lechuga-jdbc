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

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Empresa_.class, Empleado_.class,
                EmpresaEmpleado_.class);

        Service service = TransactionalServiceProxyfier.proxyfy(facade, new ServiceImpl(emf), Service.class);

        Empresa aedea = new Empresa(null, "AEDEA");
        Empresa lifting = new Empresa(null, "LIFTING");

        Empleado serr = new Empleado(null, "SERR");
        Empleado ross = new Empleado(null, "ROSS");

        Empleado ilus = new Empleado(null, "ILUS");

        List<Triad<Empresa, ERolEmpleado, Empleado>> d = new ArrayList<>();
        d.add(new Triad<>(aedea, ERolEmpleado.ABRILLANTADOR, serr));
        d.add(new Triad<>(aedea, ERolEmpleado.PULIDOR, ross));
        d.add(new Triad<>(lifting, ERolEmpleado.PULIDOR, ilus));

        service.store(d);
    }

    @Test
    public void testGen() throws Exception {
        String s = HsqldbDDLGenerator.generateScript(Empresa_.class, Empleado_.class, EmpresaEmpleado_.class);
        System.err.println(s);
    }

    public static interface Service {

        @TransactionalMethod
        void store(List<Triad<Empresa, ERolEmpleado, Empleado>> d);

    }

    public static class ServiceImpl implements Service {

        final EntityManager<Empresa, Short> empresaEm;
        final EntityManager<EmpresaEmpleado, EmpresaEmpleadoId> empresaEmpleadoEm;
        final EntityManager<Empleado, Long> empleadoEm;

        public ServiceImpl(IEntityManagerFactory emf) {
            super();
            this.empresaEm = emf.getEntityManager(Empresa.class);
            this.empresaEmpleadoEm = emf.getEntityManager(EmpresaEmpleado.class);
            this.empleadoEm = emf.getEntityManager(Empleado.class);
        }

        @Override
        public void store(List<Triad<Empresa, ERolEmpleado, Empleado>> d) {
            d.forEach(p -> {
                Empresa empresa = p.getA();
                ERolEmpleado rolEmpleado = p.getB();
                Empleado empleado = p.getC();

                if (empresa.getIdEmpresa() == null) {
                    empresaEm.insert(empresa);
                }
                if (empleado.getIdEmpleado() == null) {
                    empleadoEm.insert(empleado);
                }
                empresaEmpleadoEm.store(new EmpresaEmpleado(
                        new EmpresaEmpleadoId(empresa.getIdEmpresa(), empleado.getIdEmpleado()), rolEmpleado));
            });
        }

    }

    @Entity(entity = Empresa.class, table = "empresas")
    public static interface Empresa_ {

        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_empresas")
        public static final MetaField<Empresa, Long> idEmpresa = new MetaField<>("idEmpresa");
        public static final MetaField<Empresa, String> nombre = new MetaField<>("nombre");

        public static final OneToMany<Empresa, EmpresaEmpleado> empresaEmpleados = new OneToMany<>(Empresa.class,
                EmpresaEmpleado.class, new PropPair<>(Empresa_.idEmpresa, EmpresaEmpleado_.idEmpresa));
    }

    @Entity(entity = EmpresaEmpleado.class)
    public static interface EmpresaEmpleado_ {

        @Id
        public static final MetaField<EmpresaEmpleado, Long> idEmpresa = new MetaField<>("id.idEmpresa");
        @Id
        public static final MetaField<EmpresaEmpleado, Integer> idEmpleado = new MetaField<>("id.idEmpleado");

        @EnumHandler
        public static final MetaField<EmpresaEmpleado, ERolEmpleado> rolEmpleado = new MetaField<>("rolEmpleado");

        public static final ManyToOne<EmpresaEmpleado, Empresa> empresas = new ManyToOne<>(EmpresaEmpleado.class,
                Empresa.class, new PropPair<>(EmpresaEmpleado_.idEmpresa, Empresa_.idEmpresa));

        public static final ManyToOne<EmpresaEmpleado, Empleado> empleados = new ManyToOne<>(EmpresaEmpleado.class,
                Empleado.class, new PropPair<>(EmpresaEmpleado_.idEmpleado, Empleado_.idEmpleado));

    }

    @Entity(entity = Empleado.class, table = "empleados")
    public static interface Empleado_ {

        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_empleados")
        public static final MetaField<Empleado, Integer> idEmpleado = new MetaField<>("idEmpleado");
        public static final MetaField<Empleado, String> nombre = new MetaField<>("nombre");

        public static final OneToMany<Empleado, EmpresaEmpleado> empresaEmpleados = new OneToMany<>(Empleado.class,
                EmpresaEmpleado.class, new PropPair<>(Empleado_.idEmpleado, EmpresaEmpleado_.idEmpleado));

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
        Integer idEmpleado;

        public EmpresaEmpleadoId() {
            super();
        }

        public EmpresaEmpleadoId(Long idEmpresa, Integer idEmpleado) {
            super();
            this.idEmpresa = idEmpresa;
            this.idEmpleado = idEmpleado;
        }

        public Long getIdEmpresa() {
            return idEmpresa;
        }

        public void setIdEmpresa(Long idEmpresa) {
            this.idEmpresa = idEmpresa;
        }

        public Integer getIdEmpleado() {
            return idEmpleado;
        }

        public void setIdEmpleado(Integer idEmpleado) {
            this.idEmpleado = idEmpleado;
        }

        @Override
        public String toString() {
            return "EmpresaEmpleadoId [idEmpresa=" + idEmpresa + ", idEmpleado=" + idEmpleado + "]";
        }

    }

    public static class EmpresaEmpleado {
        EmpresaEmpleadoId id;
        ERolEmpleado rolEmpleado;

        public EmpresaEmpleado() {
            super();
        }

        public EmpresaEmpleado(EmpresaEmpleadoId id, ERolEmpleado rolEmpleado) {
            super();
            this.id = id;
            this.rolEmpleado = rolEmpleado;
        }

        public EmpresaEmpleadoId getId() {
            return id;
        }

        public void setId(EmpresaEmpleadoId id) {
            this.id = id;
        }

        public ERolEmpleado getRolEmpleado() {
            return rolEmpleado;
        }

        public void setRolEmpleado(ERolEmpleado rolEmpleado) {
            this.rolEmpleado = rolEmpleado;
        }

        @Override
        public String toString() {
            return "EmpresaEmpleado [id=" + id + ", rolEmpleado=" + rolEmpleado + "]";
        }

    }

    public static class Empleado {
        Integer idEmpleado;
        String nombre;

        public Empleado() {
            super();
        }

        public Empleado(Integer idEmpleado, String nombre) {
            super();
            this.idEmpleado = idEmpleado;
            this.nombre = nombre;
        }

        public Integer getIdEmpleado() {
            return idEmpleado;
        }

        public void setIdEmpleado(Integer idEmpleado) {
            this.idEmpleado = idEmpleado;
        }

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        @Override
        public String toString() {
            return "Empleado [idEmpleado=" + idEmpleado + ", nombre=" + nombre + "]";
        }

    }

    public static enum ERolEmpleado {
        PULIDOR, ABRILLANTADOR;
    }

}
