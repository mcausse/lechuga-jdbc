package typesafecriteria.ent;

import org.lechuga.annotated.anno.Entity;
import org.lechuga.mapper.MetaField;

@Entity(entity = DeptCount.class, table = "")
public interface DeptCount_ extends Department_ {

    public static final MetaField<DeptCount, Long> employees = new MetaField<>("employees");

}