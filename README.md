# Lechuga JDBC

```java
	public class Department {
		Long id;
		String name;
		...
	}    
```
```java
	public class EmployeeId {
		Long idDepartment;
		String dni;
		...
	}
	
	public enum ESex { MALE, FEMALE; }
	
	@Table("employees")
	public class Employee {
		EmployeeId id;
		String name;
		Double salary;
		String birthDate;
		ESex sex;
		...
	}
```

```java
	@Entity(entity = Department.class, table = "departments")
	public interface Department_ {

		@Generated(value = HsqldbSequence.class, args = "seq_department")
		@Id
		public static final MetaField<Department, Long> id = new MetaField<>("id");

		@Column("dept_name")
		public static final MetaField<Department, String> name = new MetaField<>("name");

		public static final OneToMany<Department, Employee> employees = new OneToMany<>(
			Department.class, Employee.class,
			new PropPair<>(Department_.id, Employee_.idDept));
	}
	
	@Entity(entity = Employee.class, table = "employees")
	public interface Employee_ {

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

		public static final ManyToOne<Employee, Department> department = new ManyToOne<>(
			Employee.class, Department.class,
			new PropPair<>(Employee_.idDept, Department_.id));
	}
```

## EntityManager	
	
```java
	DataSource ds = ...
	DataAccesFacade facade = new JdbcDataAccesFacade(ds);
	
	IEntityManagerFactory emf = new EntityManagerFactory(facade, Department_.class, Employee_.class);
	EntityManager<Employee, EmployeeId> empMan = emf.buildEntityManager(Employee.class);
	EntityManager<Department, Long> deptMan = emf.buildEntityManager(Department.class);
```

```java
	facade.begin();
	try {

		Department d = new Department();
		d.setName("Java dept.");
		deptMan.insert(d);
		
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
	List<Employee> es = empMan.loadByProp(Employee_.sex, ESex.MALE, Order.asc(Employee_.birthDate));
```
```sql
	select birth_date,dni,id_department,name,salary,sex from employees 
	where sex=? order by birth_date asc -- [MALE(String)]
```

```java
	Restrictions<Employee> r = emf.getRestrictions(Employee.class);
	List<Employee> es = empMan.loadBy(
		Restrictions.and(
			r.isNotNull(Employee_.name),
			r.between(Employee_.birthDate, "01/01/1800", "01/01/1900")
		)
		, Order.asc(Employee_.salary));
```
```sql
	select birth_date,dni,id_department,name,salary,sex from employees 
	where name is not null and birth_date between ? and ? order by salary asc  
	-- [Wed Jan 01 00:00:00 CET 1800(Date), Mon Jan 01 00:00:00 CET 1900(Date)]
```

## AAAAA

```java
	Department d = ...
	Employee e = ...
	
	List<Employee> es = Department_.employees.load(emf, d);
	
	Department d2 = Employee_.department.load(emf, e);
	
	assertEquals(
	        "[Employee [id=EmployeeId [idDepartment=101, dni=8P], name=jbm2, salary=38000.0, birthDate=22/05/1837, sex=MALE]]",
	        es.toString());
	assertEquals("Department [id=101, name=Java dept.2]", d2.toString());
```


```sql

	select r.id_department,r.dni,r.le_name,r.salary,r.birth_date,r.sex 
	from employees r join departments s on s.id=r.id_department 
	where s.id=? 
	order by r.id_department asc, r.dni asc  
	-- [101(Long)]
	
	select r.id,r.dept_name 
	from departments r 
	join employees s on s.id_department=r.id 
	where s.id_department=? and s.dni=? 
	order by r.id asc  
	-- [101(Long), 8P(String)]

```

## TypeSafeCriteria

```java
	CriteriaBuilder c = emf.createCriteria();
	
	Restrictions<Employee> re = emf.getRestrictions(Employee.class, "e");
	Restrictions<Department> rd = emf.getRestrictions(Department.class, "d");
	
	c.append("select {} from {} ", re.all(), re.table());
	c.append("join {} on {} ", rd.table(), re.eq(Employee_.idDept, rd, Department_.id));
	c.append("where {} ", Restrictions.and(rd.ge(Department_.id, 100L), rd.lt(Department_.id, 999L)));
	c.append("and {} ", re.ilike(Employee_.name, ELike.CONTAINS, "b"));
	c.append("and {} ", re.in(Employee_.sex, ESex.FEMALE, ESex.MALE));
```

```sql
	select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex 
	from employees e join departments d on e.id_department=d.id 
	where d.id>=? and d.id<? and upper(e.name) like upper(?) and e.sex in (?,?) 
	 -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]
```


## GenericDao<E, ID>

```java

	@Entity(entity = DeptCount.class, table = "")
	public interface DeptCount_ extends Department_ {
	
	    public static final MetaField<DeptCount, Long> employees = new MetaField<>("employees");
	
	}

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
```
