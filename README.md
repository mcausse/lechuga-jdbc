# Lechuga JDBC

```java
	@Table("departments")
	public class Department {
	
		@Id
		@Generated(value = HsqldbSequence.class, args = "seq_department")
		Long id;
		
		@Column("dept_name")
		String name;
		...
	}    
```
```java
	public class EmployeeId {
	
		@Id
		Long idDepartment;
		
		@Id
		String dni;
		...
	}
	
	public enum ESex { MALE, FEMALE; }
	
	@Table("employees")
	public class Employee {
	
		EmployeeId id;
		
		String name;
		
		Double salary;
		
		@CustomHandler(value = StringDateHandler.class, args = "dd/MM/yyyy")
		String birthDate;
		
		@EnumHandler
		ESex sex;
		...
	}
```

## EntityManager	
	
```java
	DataSource ds = ...
	DataAccesFacade facade = new JdbcDataAccesFacade(ds);
	
	EntityManagerFactory emf = new EntityManagerFactory(facade);
	EntityManager<Employee, EmployeeId> empMan = emf.build(Employee.class, EmployeeId.class);
	EntityManager<Department, Long> deptMan = emf.build(Department.class, Long.class);
```

```java
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
		
		facade.commit();
	} catch (Exception e) {
		facade.rollback();
		throw e;
	}
```
```sql
	call next value for seq_department -- []
	insert into departments (id,name) values (?,?) -- [100(Long), Java dept.(String)]
	insert into employees (birth_date,dni,id_department,name,salary,sex) values (?,?,?,?,?,?) 
	-- [Mon May 22 00:00:00 CET 1837(Date), 8P(String), 100(Long), jbm(String), 38000.0(Double), MALE(String)]
```

```java
	Employee es = empMan.loadById(e.getId());
```
```sql
	select birth_date,dni,id_department,name,salary,sex 
	from employees where dni=? and id_department=? 
	-- [8P(String), 100(Long)]
```

```java
	e.setSalary(38000.0);
	empMan.update(e);
	empMan.update(e, "salary");
```
```sql
	update employees set birth_date=?,name=?,salary=?,sex=? 
	where dni=? and id_department=? 
	-- [Mon May 22 00:00:00 CET 1837(Date), jbm(String), 38000.0(Double), MALE(String), 8P(String), 100(Long)]
	update employees set salary=? where dni=? and id_department=? 
	-- [38000.0(Double), 8P(String), 100(Long)]
```

```java
	List<Employee> es = empMan.loadByProp("sex", ESex.MALE, Order.asc("birthDate"));
```
```sql
	select birth_date,dni,id_department,name,salary,sex from employees 
	where sex=? order by birth_date asc -- [MALE(String)]
```

```java
	Restrictions r = empMan.getRestrictions();
	List<Employee> es = empMan.loadBy(
        Restrictions.and(
                r.isNotNull("name"),
                r.between("birthDate", "01/01/1800", "01/01/1900")
        ), 
        Order.asc("salary"));
```
```sql
	select birth_date,dni,id_department,name,salary,sex from employees 
	where name is not null and birth_date between ? and ? order by salary asc  
	-- [Wed Jan 01 00:00:00 CET 1800(Date), Mon Jan 01 00:00:00 CET 1900(Date)]
```

                
## QueryBuilder	

```java
	QueryBuilder<Employee> q = empMan.createQuery("e");
	q.addEm("d", deptMan);
	
	q.append("select {e.*} from {e} ");
	q.append("join {d} on {e.id.idDepartment}={d.id} ");
	q.append("where {d.id>=?} and {d.id<?} ", 100, 999);
	q.append("and {e.name like ?} ", "%b%");
	q.append("and {e.sex in (?,?)} ", ESex.FEMALE, ESex.MALE);

```

```sql
	select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex 
	from employees e join departments d on e.id_department=d.id 
	where d.id>=? and d.id<? and e.name like ? and e.sex in (?,?) 
	 -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]
```

## Criteria

```java
	CriteriaBuilder c = empMan.createCriteria();
	
	Restrictions re = empMan.getRestrictions("e");
	Restrictions rd = deptMan.getRestrictions("d");
	
	c.append("select {} from {} ", re.all(), re.table());
	c.append("join {} on {} ", rd.table(), re.eq("id.idDepartment", rd, "id"));
	c.append("where {} ", Restrictions.and(rd.ge("id", 100), rd.lt("id", 999)));
	c.append("and {} ", re.ilike("name", ELike.CONTAINS, "b"));
	c.append("and {} ", re.in("sex", ESex.FEMALE, ESex.MALE));
```

```sql
	select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex 
	from employees e join departments d on e.id_department=d.id 
	where d.id>=? and d.id<? and upper(e.name) like upper(?) and e.sex in (?,?) 
	 -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]
```


## GenericDao<E, ID>

```java

    public static class DepartmentDao extends GenericDao<Department, Long> {

        public DepartmentDao(DataAccesFacade facade) {
            super(facade);
        }

        public Department findDepartment(String name) {
            Restrictions r = getRestrictions();
            CriteriaBuilder c = createCriteria();
            c.append("select {} from {} ", r.all(), r.table());
            c.append("where {}", r.ilike("name", ELike.CONTAINS, name));
            return c.getExecutor(getEntityManager()).loadUnique();
        }
    }

    public static class EmployeeDao extends GenericDao<Employee, EmployeeId> {

        public EmployeeDao(DataAccesFacade facade) {
            super(facade);
        }

        public List<Employee> loadEmployeesOf(Department dept) {
            QueryBuilder<Employee> q = createQuery("e");
            q.append("select {e.*} from {e} where {e.id.idDepartment=?} ", dept.getId());
            q.append("order by {e.id.dni} asc");
            return q.getExecutor().load();
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

        DeptEmpsDto r = service.findDeptByName("ava");

        assertEquals("DeptEmpsDto [dept=Department [id=100, name=Java dept.], " + //
                "emps=[Employee [id=EmployeeId [idDepartment=100, dni=8P], name=jbm, salary=38000.0, birthDate=22/05/1837, sex=MALE]]]",
                r.toString());
    }
```
