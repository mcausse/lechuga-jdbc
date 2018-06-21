package typesafecriteria;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.GenericDao;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.CriteriaExecutor;
import org.lechuga.annotated.criteria.ELike;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.extractor.MapResultSetExtractor;
import org.lechuga.jdbc.extractor.Pager;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.HsqldbDDLGenerator;
import org.lechuga.mapper.Order;

import typesafecriteria.ent.Department;
import typesafecriteria.ent.Department_;
import typesafecriteria.ent.DeptCount;
import typesafecriteria.ent.DeptCount_;
import typesafecriteria.ent.ESex;
import typesafecriteria.ent.Employee;
import typesafecriteria.ent.EmployeeId;
import typesafecriteria.ent.Employee_;

public class TypeSafeCriteriaTest {

    @Test
    public void testGen() throws Exception {

        String r = HsqldbDDLGenerator.generateScript(Employee_.class, Department_.class);
        System.err.println(r);
    }

    @Test
    public void testName2() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(null, Employee_.class, Department_.class);

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
            sql.runFromClasspath("employees.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testManyToOneOneToMany() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Department_.class, Employee_.class);
        EntityManager<Employee, EmployeeId> empMan = emf.buildEntityManager(Employee.class);
        EntityManager<Department, Long> deptMan = emf.buildEntityManager(Department.class);

        facade.begin();
        try {

            Department d = new Department();
            d.setName("DB dept.");
            deptMan.store(d);

            Employee e = new Employee();
            e.setId(new EmployeeId(d.getId(), "8P"));
            e.setName("jor");
            e.setSalary(38000.0);
            e.setBirthDate("22/05/1837");
            e.setSex(ESex.MALE);
            empMan.store(e);

            d = new Department();
            d.setName("Java dept.2");
            deptMan.store(d);

            e = new Employee();
            e.setId(new EmployeeId(d.getId(), "8P"));
            e.setName("jbm2");
            e.setSalary(38000.0);
            e.setBirthDate("22/05/1837");
            e.setSex(ESex.MALE);
            empMan.store(e);

            // select id_department,dni,le_name,salary,birth_date,sex from employees join
            // departments on id=id_department where id=? order by id_department asc, dni
            // asc -- [101(Long)]
            // select id,dept_name from departments join employees on id_department=id where
            // id_department=? and dni=? order by id asc -- [101(Long), 8P(String)]

            List<Employee> es = Department_.employees.load(emf, d);

            Department d2 = Employee_.department.load(emf, e);

            assertEquals(
                    "[Employee [id=EmployeeId [idDepartment=101, dni=8P], name=jbm2, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                    es.toString());
            assertEquals("Department [id=101, name=Java dept.2]", d2.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

    }

    @Test
    public void testName() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Department_.class, Employee_.class);
        EntityManager<Employee, EmployeeId> empMan = emf.buildEntityManager(Employee.class);
        EntityManager<Department, Long> deptMan = emf.buildEntityManager(Department.class);

        facade.begin();
        try {

            Department d = new Department();
            d.setName("Java dept.");
            deptMan.store(d);
            deptMan.store(d);

            Employee e = new Employee();
            e.setId(new EmployeeId(d.getId(), "8P"));
            e.setName("jbm");
            e.setSalary(38000.0);
            e.setBirthDate("22/05/1837");
            e.setSex(ESex.MALE);

            empMan.insert(e);
            empMan.store(e);

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
                Employee r = empMan.loadById(e.getId());
                assertEquals(
                        "Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]",
                        r.toString());
            }

            {
                List<Employee> es = empMan.loadByProp(Employee_.sex, ESex.MALE, Order.asc(Employee_.birthDate));
                assertEquals(
                        "[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
                        es.toString());
            }
            {
                Employee es = empMan.loadUniqueByProp(Employee_.sex, ESex.MALE);
                assertEquals(
                        "Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]",
                        es.toString());
            }

            {
                List<Employee> es = empMan.loadAll(Order.asc(Employee_.birthDate), Order.desc(Employee_.dni));
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
                Restrictions<Employee> r = emf.getRestrictions(Employee.class);
                Employee es = empMan.loadUniqueBy( //
                        Restrictions.and( //
                                r.isNotNull(Employee_.name), //
                                r.between(Employee_.birthDate, "01/01/1800", "01/01/1900") //
                        ) //
                        , Order.asc(Employee_.salary));

                assertEquals(
                        "Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]",
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

            {
                CriteriaBuilder c = emf.createCriteria();
                Restrictions<Employee> r = emf.getRestrictions(Employee.class);
                c.append("select {} from {}", r.all(), r.table());

                CriteriaExecutor<Employee> exec = c.getExecutor(Employee.class);
                List<Map<String, Object>> m = exec.extract(new MapResultSetExtractor());

                assertEquals(
                        "[{ID_DEPARTMENT=100, DNI=8P, LE_NAME=jbm, SALARY=38000, BIRTH_DATE=1837-05-22 00:00:00.0, SEX=MALE}]",
                        m.toString());
            }
            {
                assertTrue(empMan.exists(e));
                assertTrue(empMan.existsById(e.getId()));
                assertTrue(deptMan.exists(d));
                assertTrue(deptMan.existsById(d.getId()));

                empMan.deleteById(e.getId());
                deptMan.delete(d);

                assertFalse(empMan.exists(e));
                assertFalse(empMan.existsById(e.getId()));
                assertFalse(deptMan.exists(d));
                assertFalse(deptMan.existsById(d.getId()));

            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

    }

    @Test
    public void testPager() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Department_.class, Employee_.class);
        EntityManager<Employee, EmployeeId> empMan = emf.buildEntityManager(Employee.class);
        EntityManager<Department, Long> deptMan = emf.buildEntityManager(Department.class);

        facade.begin();
        try {

            Department d = new Department();
            d.setName("Java dept.");
            deptMan.store(d);
            deptMan.store(d);

            for (int i = 0; i < 5; i++) {
                Employee e = new Employee();
                e.setId(new EmployeeId(d.getId(), i + "P"));
                e.setName("jbm" + i);
                e.setSalary(38000.0);
                e.setBirthDate("22/05/1837");
                e.setSex(ESex.MALE);
                empMan.store(e);
            }

            CriteriaBuilder c = emf.createCriteria();
            Restrictions<Employee> r = emf.getRestrictions(Employee.class);
            c.append("select {} from {}", r.all(), r.table());
            CriteriaExecutor<Employee> exec = c.getExecutor(Employee.class);

            Pager<Employee> p0 = exec.loadPage(3, 0);
            Pager<Employee> p1 = exec.loadPage(3, 1);
            Pager<Employee> p2 = exec.loadPage(3, 2);

            assertEquals(
                    "Pager [pageSize=3, numPage=0, totalRows=5, totalPages=2, page=[Employee [id=EmployeeId [idDepartment=100, dni=0P], name=jbm0, salary=38000.0, birthDate=22/05/1837, sex=MALE], Employee [id=EmployeeId [idDepartment=100, dni=1P], name=jbm1, salary=38000.0, birthDate=22/05/1837, sex=MALE], Employee [id=EmployeeId [idDepartment=100, dni=2P], name=jbm2, salary=38000.0, birthDate=22/05/1837, sex=MALE]]]",
                    p0.toString());
            assertEquals(
                    "Pager [pageSize=3, numPage=1, totalRows=5, totalPages=2, page=[Employee [id=EmployeeId [idDepartment=100, dni=3P], name=jbm3, salary=38000.0, birthDate=22/05/1837, sex=MALE], Employee [id=EmployeeId [idDepartment=100, dni=4P], name=jbm4, salary=38000.0, birthDate=22/05/1837, sex=MALE]]]",
                    p1.toString());
            assertEquals("Pager [pageSize=3, numPage=2, totalRows=5, totalPages=2, page=[]]", p2.toString());

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

    public static class DepartmentDao extends GenericDao<Department, Long> {

        public DepartmentDao(IEntityManagerFactory emf) {
            super(emf);
        }

        public Department findDepartment(String name) {

            CriteriaBuilder c = emf.createCriteria();
            Restrictions<Department> r = emf.getRestrictions(Department.class, "d");
            c.append("select {} from {} ", r.all(), r.table());
            c.append("where {}", r.ilike(Department_.name, ELike.CONTAINS, name));
            return c.getExecutor(Department.class).loadUnique();
        }

        public List<DeptCount> getDeptCounts() {

            CriteriaBuilder c = emf.createCriteria();

            Restrictions<DeptCount> r = emf.getRestrictions(DeptCount.class, "r");
            Restrictions<Department> d = emf.getRestrictions(Department.class, "d");
            Restrictions<Employee> e = emf.getRestrictions(Employee.class, "e");

            c.append("select {}, count(*) as {} ", d.all(), r.column(DeptCount_.employees));
            c.append("from {} join {} ", d.table(), e.table());
            c.append("on {} ", d.eq(Department_.id, e, Employee_.idDept));
            c.append("group by {} ", d.all());
            c.append("order by {}", d.orderBy(Order.asc(Department_.id)));

            return c.getExecutor(DeptCount.class).load();
        }
    }

    public static class EmployeeDao extends GenericDao<Employee, EmployeeId> {

        public EmployeeDao(IEntityManagerFactory emf) {
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

        public TestService(IEntityManagerFactory emf) {
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

        public List<DeptCount> getDeptCounts() {
            facade.begin();
            try {
                return departmentDao.getDeptCounts();
            } finally {
                facade.rollback();
            }
        }

    }

    @Test
    public void testService() throws Exception {
        IEntityManagerFactory emf = new EntityManagerFactory(facade, Department_.class, Employee_.class,
                DeptCount_.class);
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

        assertEquals("[DeptCount [employees=1, id=100, name=Java dept.]]", service.getDeptCounts().toString());
    }
}
