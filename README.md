# Frijoles JDBC

```java
	@Table("departments")
	public class Department {
		@Id
		@Generated(value = HsqldbSequence.class, args = "seq_department")
		Long id;
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

	=> select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex 
	from employees e join departments d on e.id_department=d.id 
	where d.id>=? and d.id<? and e.name like ? and e.sex in (?,?) 
	 -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]


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

	=> select e.birth_date,e.dni,e.id_department,e.name,e.salary,e.sex 
	from employees e join departments d on e.id_department=d.id 
	where d.id>=? and d.id<? and upper(name) like upper(?) and sex in (?,?) 
	 -- [100(Integer), 999(Integer), %b%(String), FEMALE(String), MALE(String)]
	
	