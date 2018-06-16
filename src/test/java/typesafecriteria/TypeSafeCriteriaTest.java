package typesafecriteria;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.annotated.criteria.ELike;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.HsqldbDDLGenerator;

import typesafecriteria.ent.Department;
import typesafecriteria.ent.Department_;
import typesafecriteria.ent.ESex;
import typesafecriteria.ent.Employee;
import typesafecriteria.ent.EmployeeId;
import typesafecriteria.ent.Employee_;

public class TypeSafeCriteriaTest {

    @Test
    public void testName2() throws Exception {

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
        assertEquals(
                "select e.id_department,e.dni,e.le_name,e.salary,e.birth_date,e.sex from employees e where e.salary between ? and ? or e.birth_date>? or e.dni=? or e.sex in (?,?)  -- [10000.0(Float), 20000.0(Float), Sun Jan 01 00:00:00 CET 1301(Date), 8P(String), FEMALE(String), MALE(String)]",
                c.toString());
    }

    final DataAccesFacade facade;

    public TypeSafeCriteriaTest() {
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
            sql.runFromClasspath("employees2.sql");
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

        EntityManagerFactory2 emf = new EntityManagerFactory2(facade, Department_.class, Employee_.class);
        EntityManager<Employee, EmployeeId> empMan = emf.buildEntityManager(Employee.class);
        EntityManager<Department, Long> deptMan = emf.buildEntityManager(Department.class);

        facade.begin();
        try {

            Department d = new Department();
            d.setName("Java dept.");
            deptMan.store(d);

            Employee e = new Employee();
            e.setId(new EmployeeId(d.getId(), "8P"));
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
                List<Employee> es = empMan.loadByProp(Employee_.sex, ESex.MALE, Order.asc(Employee_.birthDate));
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        es.toString());
            }
            {
                Restrictions<Employee> r = emf.getRestrictions(Employee.class);
                List<Employee> es = empMan.loadBy( //
                        Restrictions.and( //
                                r.isNotNull(Employee_.name), //
                                r.between(Employee_.birthDate, "01/01/1800", "01/01/1900") //
                        ) //
                        , Order.asc(Employee_.salary));

                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        es.toString());
            }

            {

                CriteriaBuilder c = emf.createCriteria();
                Restrictions<Employee> re = emf.getRestrictions(Employee.class, "e");
                Restrictions<Department> rd = emf.getRestrictions(Department.class, "d");

                c.append("select {} from {} ", re.all(), re.table());
                c.append("join {} on {} ", rd.table(), re.eq(Employee_.idDept, rd, Department_.id));
                c.append("where {} ", Restrictions.and( //
                        rd.ge(Department_.id, 100L), //
                        rd.lt(Department_.id, 999L) //
                ));
                c.append("and {} ", re.like(Employee_.name, ELike.CONTAINS, "b"));
                c.append("and {}", re.in(Employee_.sex, ESex.FEMALE, ESex.MALE));

                assertEquals(
                        "select e.id_department,e.dni,e.le_name,e.salary,e.birth_date,e.sex from employees e join departments d on e.id_department=d.id where d.id>=? and d.id<? and e.le_name like ? and e.sex in (?,?) -- [100(Long), 999(Long), %b%(String), FEMALE(String), MALE(String)]",
                        c.toString());

                List<Employee> r = c.getExecutor(Employee.class).load();
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        r.toString());

            }
            {
                CriteriaBuilder c = emf.createCriteria();

                Restrictions<Employee> re = emf.getRestrictions(Employee.class, "e");
                Restrictions<Department> rd = emf.getRestrictions(Department.class, "d");

                c.append("select {} from {} ", re.all(), re.table());
                c.append("join {} on {} ", rd.table(), re.eq(Employee_.idDept, rd, Department_.id));
                c.append("where {} ", Restrictions.and(rd.ge(Department_.id, 100L), rd.lt(Department_.id, 999L)));
                c.append("and {} ", re.ilike(Employee_.name, ELike.CONTAINS, "b"));
                c.append("and {} ", re.in(Employee_.sex, ESex.FEMALE, ESex.MALE));

                assertEquals(
                        "select e.id_department,e.dni,e.le_name,e.salary,e.birth_date,e.sex from employees e join departments d on e.id_department=d.id where d.id>=? and d.id<? and upper(e.le_name) like upper(?) and e.sex in (?,?)  -- [100(Long), 999(Long), %b%(String), FEMALE(String), MALE(String)]",
                        c.toString());

                List<Employee> r = c.getExecutor(Employee.class).load();
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        r.toString());
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

    }

    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////////////////

    public static class DeptCount {

        Department dept;
        long employees;

        public Department getDept() {
            return dept;
        }

        public void setDept(Department dept) {
            this.dept = dept;
        }

        public long getEmployees() {
            return employees;
        }

        public void setEmployees(long employees) {
            this.employees = employees;
        }

        @Override
        public String toString() {
            return "DeptCount [dept=" + dept + ", employees=" + employees + "]";
        }

    }

    public static class DepartmentDao extends GenericDao<Department, Long> {

        public DepartmentDao(EntityManagerFactory2 emf) {
            super(emf);
        }

        public Department findDepartment(String name) {

            CriteriaBuilder c = emf.createCriteria();
            Restrictions<Department> r = emf.getRestrictions(Department.class, "d");
            c.append("select {} from {} ", r.all(), r.table());
            c.append("where {}", r.ilike(Department_.name, ELike.CONTAINS, name));
            return c.getExecutor(Department.class).loadUnique();
        }

        // // TODO
        // public List<DeptCount> getDeptCounts() {
        //
        // QueryBuilder<DeptCount> q = emf.createQuery(DeptCount.class, "r");
        // q.addEm("d", Department.class);
        // q.addEm("e", Employee.class);
        //
        // q.append("select {d.*}, count(*) as {r.employees} ");
        // q.append("from {d} join {e} on {d.id}={e.id.idDepartment} ");
        // q.append("group by {d.*} ");
        //
        // return q.getExecutor().load();
        // }
    }

    public static class EmployeeDao extends GenericDao<Employee, EmployeeId> {

        public EmployeeDao(EntityManagerFactory2 emf) {
            super(emf);
        }

        public List<Employee> loadEmployeesOf(Department dept) {

            CriteriaBuilder c = emf.createCriteria();
            Restrictions<Employee> r = emf.getRestrictions(Employee.class, "e");

            c.append("select {} from {} ", r.all(), r.table());
            c.append("where {} ", r.eq(Employee_.idDept, dept.getId()));
            c.append("order by {}", r.orderBy(Order.asc(Employee_.dni)));

            return c.getExecutor(Employee.class).load();
        }
    }

    public static class DeptEmpsDto {

        final Department dept;
        final List<Employee> emps;

        public DeptEmpsDto(Department dept, List<Employee> emps) {
            super();
            this.dept = dept;
            this.emps = emps;
        }

        public Department getDept() {
            return dept;
        }

        public List<Employee> getEmps() {
            return emps;
        }

        @Override
        public String toString() {
            return "DeptEmpsDto [dept=" + dept + ", emps=" + emps + "]";
        }

    }

    public static class TestService {

        final DataAccesFacade facade;
        final DepartmentDao departmentDao;
        final EmployeeDao employeeDao;

        public TestService(EntityManagerFactory2 emf) {
            super();
            this.facade = emf.getFacade();
            this.departmentDao = new DepartmentDao(emf);
            this.employeeDao = new EmployeeDao(emf);
        }

        public void create(Department dept, Collection<Employee> employees) {
            facade.begin();
            try {

                departmentDao.store(dept);
                employees.forEach(e -> e.getId().setIdDepartment(dept.getId()));
                employeeDao.store(employees);

                facade.commit();
            } catch (Throwable e) {
                facade.rollback();
                throw new RuntimeException(e);
            }
        }

        public DeptEmpsDto findDeptByName(String name) {
            facade.begin();
            try {
                Department dept = departmentDao.findDepartment(name);
                List<Employee> emps = employeeDao.loadEmployeesOf(dept);
                return new DeptEmpsDto(dept, emps);
            } finally {
                facade.rollback();
            }
        }

        // TODO
        // public List<DeptCount> getDeptCounts() {
        // facade.begin();
        // try {
        // return departmentDao.getDeptCounts();
        // } finally {
        // facade.rollback();
        // }
        // }

    }

    @Test
    public void testService() throws Exception {
        EntityManagerFactory2 emf = new EntityManagerFactory2(facade, Department_.class, Employee_.class);
        TestService service = new TestService(emf);

        {
            Department d = new Department();
            d.setName("Java dept.");

            Employee e = new Employee();
            e.setId(new EmployeeId());
            e.getId().setIdDepartment(d.getId());
            e.getId().setDni("8P");
            e.setName("jbm");
            e.setSalary(38000.0);
            e.setBirthDate("22/05/1837");
            e.setSex(ESex.MALE);

            service.create(d, Arrays.asList(e));
        }

        DeptEmpsDto r = service.findDeptByName("ava");
        assertEquals("DeptEmpsDto [dept=Department [id=100, name=Java dept.], " + //
                "emps=[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]]",
                r.toString());

        // TODO
        // assertEquals("[DeptCount [dept=Department [id=100, name=Java dept.],
        // employees=1]]",
        // service.getDeptCounts().toString());
    }
}
