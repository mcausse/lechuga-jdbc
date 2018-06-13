package example;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.GenericDao;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.annotated.anno.Column;
import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.annotated.anno.Table;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.HsqldbDDLGenerator;
import org.lechuga.mapper.Order;
import org.lechuga.mapper.autogen.HsqldbSequence;
import org.lechuga.mapper.criteria.CriteriaBuilder;
import org.lechuga.mapper.criteria.ELike;
import org.lechuga.mapper.criteria.Restrictions;
import org.lechuga.mapper.handler.custom.StringDateHandler;
import org.lechuga.mapper.query.QueryBuilder;

public class Example {

    public static enum ESex {
        MALE, FEMALE;
    }

    // TODO experrimental
    // @NamedQuery(name = "get-dept-employees", uniqueResult = false, //
    // resultAlias = @NamedQueryAlias(alias = "e", entityClass = Employee.class), //
    // aliases = { @NamedQueryAlias(alias = "d", entityClass = Department.class) },
    // //
    // query = "select {e.*} from {e} where {e.id.idDepartment}={d.id}")
    @Table("departments")
    public static class Department {

        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_department")
        Long id;

        @Column("dept_name")
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

    final DataAccesFacade facade;

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

            empMan.insert(e);

            {
                Employee es = empMan.loadById(e.getId());
                assertEquals(
                        "Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]",
                        es.toString());

                e.setSalary(38000.0);
                empMan.update(e);
                empMan.update(e, "salary");
            }

            {
                List<Employee> es = empMan.loadByProp("sex", ESex.MALE, Order.asc("birthDate"));
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        es.toString());
            }
            {
                Restrictions r = empMan.getRestrictions();
                List<Employee> es = empMan.loadBy( //
                        Restrictions.and( //
                                r.isNotNull("name"), //
                                r.between("birthDate", "01/01/1800", "01/01/1900") //
                        ) //
                        , Order.asc("salary"));

                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        es.toString());
            }

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
                        "select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex from employees e join departments d on e.id_department=d.id where d.id>=? and d.id<? and upper(e.name) like upper(?) and e.sex in (?,?)  -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]",
                        c.toString());

                List<Employee> r = c.getExecutor(empMan).load();
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        r.toString());
            }

            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }

    }

    @Test
    public void testService() throws Exception {
        TestService service = new TestService(facade);

        {
            Department d = new Department();
            d.setName("Java dept.");

            Employee e = new Employee();
            e.setId(new EmployeeId());
            e.getId().setIdDepartment(d.id);
            e.getId().setDni("8P");
            e.setName("jbm");
            e.setSalary(38000.0);
            e.setBirthDate("22/05/1837");
            e.setSex(ESex.MALE);

            service.create(d, Arrays.asList(e));
        }

        Department dept = service.findDepartment("ava");
        assertEquals("Department [id=100, name=Java dept.]", dept.toString());

        List<Employee> es = service.loadEmployeesOf(dept);
        assertEquals(
                "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                es.toString());
    }

    public static class DepartmentDao extends GenericDao<Department, Long> {

        public DepartmentDao(DataAccesFacade facade) {
            super(facade);
        }
    }

    public static class EmployeeDao extends GenericDao<Employee, EmployeeId> {

        public EmployeeDao(DataAccesFacade facade) {
            super(facade);
        }
    }

    public static class TestService {

        final DataAccesFacade facade;
        final DepartmentDao departmentDao;
        final EmployeeDao employeeDao;

        public TestService(DataAccesFacade facade) {
            super();
            this.facade = facade;
            this.departmentDao = new DepartmentDao(facade);
            this.employeeDao = new EmployeeDao(facade);
        }

        public void create(Department dept, Collection<Employee> employees) {
            facade.begin();
            try {

                departmentDao.store(dept);
                employees.forEach(e -> e.getId().setIdDepartment(dept.getId()));
                employeeDao.store(employees);

                facade.commit();
            } catch (Exception e) {
                facade.rollback();
                throw new RuntimeException(e);
            }
        }

        public Department findDepartment(String name) {
            facade.begin();
            try {
                Restrictions r = departmentDao.getRestrictions();
                CriteriaBuilder c = departmentDao.createCriteria();
                c.append("select {} from {} ", r.all(), r.table());
                c.append("where {}", r.ilike("name", ELike.CONTAINS, name));
                return c.getExecutor(departmentDao.getEntityManager()).loadUnique();
            } finally {
                facade.rollback();
            }
        }

        public List<Employee> loadEmployeesOf(Department dept) {
            facade.begin();
            try {
                QueryBuilder<Employee> q = employeeDao.createQuery("e");
                q.append("select {e.*} from {e} where {e.id.idDepartment=?} ", dept.getId());
                q.append("order by {e.id.dni} asc");
                return q.getExecutor().load();
            } finally {
                facade.rollback();
            }
        }
    }
    
}
