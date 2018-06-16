package typesafecriteria.ent;

import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.anno.Entity;

@Entity(entity = DeptCount.class, table = "")
public interface DeptCount_ extends Department_ {

    public static final MetaField<DeptCount, Long> employees = new MetaField<>("employees");

}