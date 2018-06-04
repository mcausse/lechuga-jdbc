package org.frijoles.mapper;

public class Order {

    final String property;
    final String order;

    private Order(String property, String order) {
        super();
        this.property = property;
        this.order = order;
    }

    public static Order asc(String property) {
        return new Order(property, " asc");
    }

    public static Order desc(String property) {
        return new Order(property, " desc");
    }

    public String getProperty() {
        return property;
    }

    public String getOrder() {
        return order;
    }

    @Override
    public String toString() {
        return property + order;
    }

}
