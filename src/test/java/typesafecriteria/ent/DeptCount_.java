package typesafecriteria.ent;

import typesafecriteria.Entity;
import typesafecriteria.MetaField;

@Entity(entity = DeptCount.class, table = "")
public interface DeptCount_ extends Department_ {

    public static final MetaField<DeptCount, Long> employees = new MetaField<>("employees");

}