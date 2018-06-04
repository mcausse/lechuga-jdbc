package org.frijoles.anno;

import org.frijoles.annotated.anno.Generated;
import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;
import org.frijoles.mapper.autogen.HsqldbSequence;

@Table("pizzas")
public class Pizza {

    @Id
    @Generated(value = HsqldbSequence.class, args = "seq_pizza")
    Long id;

    String name;

    public Pizza() {
        super();
    }

    public Pizza(Long id, String name) {
        super();
        this.id = id;
        this.name = name;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "Pizza [id=" + id + ", name=" + name + "]";
    }

}
