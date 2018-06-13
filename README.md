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
		...
		deptMan.store(d);
		
		Employee e = new Employee();
		...
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

