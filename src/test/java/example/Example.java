package example;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.frijoles.annotated.EntityManagerFactory;
import org.frijoles.annotated.anno.CustomHandler;
import org.frijoles.annotated.anno.EnumHandler;
import org.frijoles.annotated.anno.Generated;
import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;
import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.JdbcDataAccesFacade;
import org.frijoles.jdbc.util.SqlScriptExecutor;
import org.frijoles.mapper.EntityManager;
import org.frijoles.mapper.HsqldbDDLGenerator;
import org.frijoles.mapper.autogen.HsqldbSequence;
import org.frijoles.mapper.criteria.CriteriaBuilder;
import org.frijoles.mapper.criteria.ELike;
import org.frijoles.mapper.criteria.Restrictions;
import org.frijoles.mapper.handler.custom.StringDateHandler;
import org.frijoles.mapper.query.QueryBuilder;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

public class Example {

    public static enum ESex {
        MALE, FEMALE;
    }

    @Table("departments")
    public static class Department {
        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_department")
        Long id;
        String name;

        public Long getId() {
            return id;
        }

        public void setId(Long id) {
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
            return "Department [id=" + id + ", name=" + name + "]";
        }

    }

    public static class EmployeeId {
        @Id
        Long idDepartment;
        @Id
        String dni;

        public Long getIdDepartment() {
            return idDepartment;
        }

        public void setIdDepartment(Long idDepartment) {
            this.idDepartment = idDepartment;
        }

        public String getDni() {
            return dni;
        }

        public void setDni(String dni) {
            this.dni = dni;
        }

        @Override
        public String toString() {
            return "EmployeeId [idDepartment=" + idDepartment + ", dni=" + dni + "]";
        }

    }

    @Table("employees")
    public static class Employee {
        EmployeeId id;
        String name;
        Double salary;
        @CustomHandler(value = StringDateHandler.class, args = "dd/MM/yyyy")
        String birthDate;
        @EnumHandler
        ESex sex;

        public EmployeeId getId() {
            return id;
        }

        public void setId(EmployeeId id) {
            this.id = id;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public Double getSalary() {
            return salary;
        }

        public void setSalary(Double salary) {
            this.salary = salary;
        }

        public String getBirthDate() {
            return birthDate;
        }

        public void setBirthDate(String birthDate) {
            this.birthDate = birthDate;
        }

        public ESex getSex() {
            return sex;
        }

        public void setSex(ESex sex) {
            this.sex = sex;
        }

        @Override
        public String toString() {
            return "Employee [id=" + id + ", name=" + name + ", salary=" + salary + ", birthDate=" + birthDate
                    + ", sex=" + sex + "]";
        }

    }

    DataAccesFacade facade;

    public Example() {
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
            sql.runFromClasspath("employees.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testGenerateSqlScript() throws Exception {
        String sql = HsqldbDDLGenerator.generateScript(Department.class, Employee.class);
        System.err.println(sql);
    }

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory(facade);
        EntityManager<Employee, EmployeeId> empMan = emf.build(Employee.class, EmployeeId.class);
        EntityManager<Department, Long> deptMan = emf.build(Department.class, Long.class);

        facade.begin();
        try {

            Department d = new Department();
            d.setName("Java dept.");
            deptMan.store(d);

            Employee e = new Employee();
            e.setId(new EmployeeId());
            e.getId().setIdDepartment(d.id);
            e.getId().setDni("8P");
            e.setName("jbm");
            e.setSalary(38000.0);
            e.setBirthDate("22/05/1837");
            e.setSex(ESex.MALE);
            empMan.store(e);

            {
                QueryBuilder<Employee> q = empMan.createQuery("e");
                q.addEm("d", deptMan);

                q.append("select {e.*} from {e} ");
                q.append("join {d} on {e.id.idDepartment}={d.id} ");
                q.append("where {d.id>=?} and {d.id<?} ", 100, 999);
                q.append("and {e.name like ?} ", "%b%");
                q.append("and {e.sex in (?,?)} ", ESex.FEMALE, ESex.MALE);

                assertEquals(
                        "select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex from employees e join departments d on e.id_department=d.id where d.id>=? and d.id<? and e.name like ? and e.sex in (?,?)  -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]",
                        q.toString());

                List<Employee> r = q.getExecutor().load();
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        r.toString());

            }
            {
                CriteriaBuilder c = empMan.createCriteria();

                Restrictions re = empMan.getRestrictions("e");
                Restrictions rd = deptMan.getRestrictions("d");

                c.append("select {} from {} ", re.all(), re.table());
                c.append("join {} on {} ", rd.table(), re.eq("id.idDepartment", rd, "id"));
                c.append("where {} ", Restrictions.and(rd.ge("id", 100), rd.lt("id", 999)));
                c.append("and {} ", re.ilike("name", ELike.CONTAINS, "b"));
                c.append("and {} ", re.in("sex", ESex.FEMALE, ESex.MALE));

                assertEquals(
                        "select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex from employees e join departments d on e.id_department=d.id where d.id>=? and d.id<? and upper(name) like upper(?) and sex in (?,?)  -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]",
                        c.toString());

                List<Employee> r = c.getExecutor(empMan).load();
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        r.toString());
            }
        } finally {
            facade.rollback();
        }
    }
}
