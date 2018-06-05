package org.frijoles.anno;

import org.frijoles.annotated.anno.Generated;
import org.frijoles.annotated.anno.Id;
import org.frijoles.annotated.anno.Table;
import org.frijoles.mapper.autogen.HsqldbSequence;

@Table("ingredients")
public class Ingredient {

    @Id
    @Generated(value = HsqldbSequence.class, args = "seq_ingredients")
    Integer id;

    String name;

    Double price;

    long idPizza;

    public Ingredient() {
        super();
    }

    public Ingredient(Integer id, String name, Double price, long idPizza) {
        super();
        this.id = id;
        this.name = name;
        this.price = price;
        this.idPizza = idPizza;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Double getPrice() {
        return price;
    }

    public void setPrice(Double price) {
        this.price = price;
    }

    public long getIdPizza() {
        return idPizza;
    }

    public void setIdPizza(long idPizza) {
        this.idPizza = idPizza;
    }

    @Override
    public String toString() {
        return "Ingredient [id=" + id + ", name=" + name + ", price=" + price + ", idPizza=" + idPizza + "]";
    }

}