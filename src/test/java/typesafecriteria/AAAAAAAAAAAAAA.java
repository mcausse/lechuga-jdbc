package typesafecriteria;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.lechuga.annotated.anno.Column;
import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.annotated.criteria.ELike;
import org.lechuga.mapper.autogen.HsqldbSequence;
import org.lechuga.mapper.handler.custom.StringDateHandler;

import example.Example.Department;
import example.Example.ESex;
import example.Example.Employee;

public class AAAAAAAAAAAAAA {

    public static class MetaField<E, T> {

        final String propertyName;

        protected MetaField(String propertyName) {
            super();
            this.propertyName = propertyName;
        }

        public String getPropertyName() {
            return propertyName;
        }

        @Override
        public String toString() {
            return "MetaField [propertyName=" + propertyName + "]";
        }

    }

    public static void main(String[] args) {
        EntityManagerFactory2 emf = new EntityManagerFactory2(null, Employee_.class, Department_.class);

        Restrictions<Employee> r = emf.getRestrictions(Employee.class);
        Restrictions<Employee> re = emf.getRestrictions(Employee.class, "e");
        Restrictions<Department> rd = emf.getRestrictions(Department.class, "d");

        System.out.println(r.eq(Employee_.sex, ESex.FEMALE));
        System.out.println(re.eq(Employee_.sex, ESex.FEMALE));
        System.out.println(re.eq(Employee_.idDept, rd, Department_.id));
        System.out.println(re.ilike(Employee_.name, ELike.CONTAINS, "ava"));

        System.out.println(r.table());
        System.out.println(re.table());
        System.out.println(rd.table());

        CriteriaBuilder c = emf.createCriteria();
        c.append("select {} from {} ", re.all(), re.table());
        c.append("where {} ", //
                Restrictions.or( //
                        re.between(Employee_.salary, 10000f, 20000f), //
                        re.gt(Employee_.birthDate, "01/01/1301"), //
                        re.eq(Employee_.dni, "8P"), //
                        re.in(Employee_.sex, ESex.FEMALE, ESex.MALE) //
                ) //
        );

        System.out.println(c);
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target(ElementType.TYPE)
    public static @interface Entity {

        Class<?> entity();

        String table() default "";
    }

    // TODO FIXME 0.3 killing-feature !!!

    @Entity(entity = Employee.class, table = "employees")
    public static interface Employee_ {

        @Id
        public static final MetaField<Employee, Long> idDept = new MetaField<>("id.idDepartment");
        @Id
        public static final MetaField<Employee, String> dni = new MetaField<>("id.dni");

        @Column("le_name")
        public static final MetaField<Employee, String> name = new MetaField<>("name");

        public static final MetaField<Employee, Float> salary = new MetaField<>("salary");

        @CustomHandler(value = StringDateHandler.class, args = "dd/MM/yyyy")
        public static final MetaField<Employee, String> birthDate = new MetaField<>("birthDate");

        @EnumHandler
        public static final MetaField<Employee, ESex> sex = new MetaField<>("sex");
    }

    @Entity(entity = Department.class, table = "departments")
    public static interface Department_ {

        @Generated(value = HsqldbSequence.class, args = "seq_department")
        @Id
        public static final MetaField<Department, Long> id = new MetaField<>("id");

        @Column("dept_name")
        public static final MetaField<Department, String> name = new MetaField<>("name");
    }

}
