package typesafecriteria.ent;

import org.lechuga.annotated.ManyToOne;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.PropPair;
import org.lechuga.annotated.anno.Column;
import org.lechuga.annotated.anno.CustomHandler;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.EnumHandler;
import org.lechuga.annotated.anno.Id;
import org.lechuga.mapper.handler.custom.StringDateHandler;

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

    public static final ManyToOne<Employee, Department> department = new ManyToOne<>(Employee.class, Department.class,
            new PropPair<>(Employee_.idDept, Department_.id));

}
