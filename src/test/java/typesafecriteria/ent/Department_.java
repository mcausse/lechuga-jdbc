package typesafecriteria.ent;

import org.lechuga.annotated.anno.Column;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.mapper.autogen.HsqldbSequence;

import typesafecriteria.Entity;
import typesafecriteria.MetaField;

@Entity(entity = Department.class, table = "departments")
public interface Department_ {

    @Generated(value = HsqldbSequence.class, args = "seq_department")
    @Id
    public static final MetaField<Department, Long> id = new MetaField<>("id");

    @Column("dept_name")
    public static final MetaField<Department, String> name = new MetaField<>("name");
}