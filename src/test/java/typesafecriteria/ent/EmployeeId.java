package typesafecriteria.ent;

public class EmployeeId {

    Long idDepartment;

    String dni;

    public EmployeeId() {
        super();
    }

    public EmployeeId(Long idDepartment, String dni) {
        super();
        this.idDepartment = idDepartment;
        this.dni = dni;
    }

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
