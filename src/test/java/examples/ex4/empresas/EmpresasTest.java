package examples.ex4.empresas;

import org.lechuga.annotated.HsqldbDDLGenerator;
import org.lechuga.annotated.ManyToOne;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.OneToMany;
import org.lechuga.annotated.PropPair;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.mapper.autogen.HsqldbSequence;

public class EmpresasTest {

    public static void main(String[] args) {
        String s = HsqldbDDLGenerator.generateScript(Empresa_.class, Empleado_.class, EmpresaEmpleado_.class);
        System.err.println(s);
    }

    // public static interface Service {

    @Entity(entity = Empresa.class, table = "empresas")
    public static interface Empresa_ {

        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_empresas")
        public static final MetaField<Empresa, Short> idEmpresa = new MetaField<>("idEmpresa");
        public static final MetaField<Empresa, String> nombre = new MetaField<>("nombre");

        public static final OneToMany<Empresa, EmpresaEmpleado> empresaEmpleados = new OneToMany<>(Empresa.class,
                EmpresaEmpleado.class,
                new PropPair<>(Empresa_.idEmpresa.castTo(Long.class), EmpresaEmpleado_.idEmpresa));
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
                Empresa.class, new PropPair<>(EmpresaEmpleado_.idEmpresa, Empresa_.idEmpresa.castTo(Long.class)));

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
        Short idEmpresa;
        String nombre;

        public Empresa() {
            super();
        }

        public Empresa(Short idEmpresa, String nombre) {
            super();
            this.idEmpresa = idEmpresa;
            this.nombre = nombre;
        }

        public Short getIdEmpresa() {
            return idEmpresa;
        }

        public void setIdEmpresa(Short idEmpresa) {
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
