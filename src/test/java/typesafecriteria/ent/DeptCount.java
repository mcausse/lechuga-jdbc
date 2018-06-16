package typesafecriteria.ent;

public class DeptCount extends Department {

    long employees;

    public long getEmployees() {
        return employees;
    }

    public void setEmployees(long employees) {
        this.employees = employees;
    }

    @Override
    public String toString() {
        return "DeptCount [employees=" + employees + ", id=" + id + ", name=" + name + "]";
    }

}