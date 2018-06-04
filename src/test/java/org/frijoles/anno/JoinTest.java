package org.frijoles.anno;

import static org.junit.Assert.assertEquals;

import java.util.List;
import java.util.Map;

import org.fijoles.annotated.EntityManagerFactory;
import org.fijoles.annotated.anno.Generated;
import org.fijoles.annotated.anno.Id;
import org.fijoles.annotated.anno.Table;
import org.fijoles.jdbc.DataAccesFacade;
import org.fijoles.jdbc.JdbcDataAccesFacade;
import org.fijoles.jdbc.extractor.MapResultSetExtractor;
import org.fijoles.jdbc.util.SqlScriptExecutor;
import org.fijoles.mapper.EntityManager;
import org.fijoles.mapper.autogen.HsqldbSequence;
import org.fijoles.mapper.query.QueryBuilder;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

public class JoinTest {

    DataAccesFacade facade;

    public JoinTest() {
        final JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:a");
        ds.setUser("sa");
        ds.setPassword("");
        this.facade = new JdbcDataAccesFacade(ds);
    }

    @Before
    public void before() {
        facade.begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(facade);
            sql.runFromClasspath("pizzas.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testName() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory(facade);
        EntityManager<Pizza, Long> pem = emf.build(Pizza.class, Long.class);
        EntityManager<Ingredient, Integer> iem = emf.build(Ingredient.class, Integer.class);

        facade.begin();
        try {

            Pizza romana = new Pizza(null, "romana");
            pem.store(romana);
            iem.store(new Ingredient(null, "base", 7.0, romana.getId()));
            iem.store(new Ingredient(null, "olivanchoa", 2.0, romana.getId()));

            Pizza margarita = new Pizza(null, "margarita");
            pem.store(margarita);
            iem.store(new Ingredient(null, "base", 7.0, margarita.getId()));

            QueryBuilder<Pizza> q = pem.createQuery("p");
            q.addEm("i", iem);
            q.append("select {p.name}, sum({i.price}) as price ");
            q.append("from {p} join {i} ");
            q.append("on {p.id}={i.idPizza} ");
            q.append("group by {p.name} ");

            assertEquals(
                    "select p.name, sum(i.price) as price from pizzas p join ingredients i on p.id=i.id_pizza group by p.name  -- []",
                    q.toString());

            List<Map<String, Object>> r = q.getExecutor().extract(new MapResultSetExtractor());
            assertEquals("[{NAME=romana, PRICE=9.0}, {NAME=margarita, PRICE=7.0}]", r.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Table("pizzas")
    public static class Pizza {

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

    @Table("ingredients")
    public static class Ingredient {

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
}