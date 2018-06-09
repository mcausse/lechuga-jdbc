package org.lechuga.mapper.ents;

public class Pizza {

    Long id;
    String name;
    double price;

    public Pizza() {
        super();
    }

    public Pizza(Long id, String name, double price) {
        super();
        this.id = id;
        this.name = name;
        this.price = price;
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

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    @Override
    public String toString() {
        return "Pizza [id=" + id + ", name=" + name + ", price=" + price + "]";
    }

}