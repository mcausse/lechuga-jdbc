package typesafecriteria.ent;

import org.lechuga.annotated.anno.Column;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.mapper.MetaField;
import org.lechuga.mapper.autogen.HsqldbSequence;

@Entity(entity = Department.class, table = "departments")
public interface Department_ {

    @Generated(value = HsqldbSequence.class, args = "seq_department")
    @Id
    public static final MetaField<Department, Long> id = new MetaField<>("id");

    @Column("dept_name")
    public static final MetaField<Department, String> name = new MetaField<>("name");
}