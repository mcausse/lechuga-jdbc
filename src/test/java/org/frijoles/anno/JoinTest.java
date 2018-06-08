package org.frijoles.anno;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.frijoles.anno.ents.Ingredient;
import org.frijoles.anno.ents.Pizza;
import org.frijoles.annotated.EntityManagerFactory;
import org.frijoles.annotated.util.Pair;
import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.JdbcDataAccesFacade;
import org.frijoles.jdbc.ResultSetUtils;
import org.frijoles.jdbc.RowMapper;
import org.frijoles.jdbc.extractor.MapResultSetExtractor;
import org.frijoles.jdbc.util.SqlScriptExecutor;
import org.frijoles.mapper.EntityManager;
import org.frijoles.mapper.query.QueryBuilder;
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
    public void testComposition() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory(facade);
        EntityManager<Pizza, Long> pem = emf.build(Pizza.class, Long.class);
        EntityManager<Ingredient, Integer> iem = emf.build(Ingredient.class, Integer.class);

        facade.begin();
        try {

            Pizza romana = new Pizza(null, "romana");
            pem.store(romana);
            iem.store(new Ingredient(null, "base", 7.0, romana.getId()));
            iem.store(new Ingredient(null, "olivanchoa", 2.0, romana.getId()));

            QueryBuilder<Ingredient> q = iem.createQuery("i");
            q.append("select {i.*} from {i} where {i.idPizza=?}", romana.getId());
            List<Ingredient> is = q.getExecutor().load();
            assertEquals(
                    "[Ingredient [id=100, name=base, price=7.0, idPizza=100], Ingredient [id=101, name=olivanchoa, price=2.0, idPizza=100]]",
                    is.toString());

            QueryBuilder<Pizza> q2 = pem.createQuery("p");
            q2.addEm("i", iem);
            q2.append("select {p.*} from {p} where {p.id=?}", is.get(0).getIdPizza());
            Pizza p = q2.getExecutor().loadUnique();
            assertEquals("Pizza [id=100, name=romana]", p.toString());

            facade.commit();
        } catch (Throwable e) {
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

            {

                QueryBuilder<Pizza> q = pem.createQuery("p");
                q.addEm("i", iem);
                q.append("select {p.name}, sum({i.price}) as price ");
                q.append("from {p} join {i} ");
                q.append("on {p.id}={i.idPizza} ");
                q.append("group by {p.name} ");

                assertEquals(
                        "select p.name, sum(i.price) as price from pizzas p join ingredients i on p.id=i.id_pizza group by p.name  -- []",
                        q.toString());

                {
                    List<Map<String, Object>> r = q.getExecutor().extract(new MapResultSetExtractor());
                    assertEquals("[{NAME=romana, PRICE=9.0}, {NAME=margarita, PRICE=7.0}]", r.toString());
                }
                {
                    List<Pair<String, Double>> r = q.getExecutor().load(new RowMapper<Pair<String, Double>>() {

                        @Override
                        public Pair<String, Double> mapRow(ResultSet rs) throws SQLException {
                            Pair<String, Double> r = new Pair<>();
                            r.setKey(ResultSetUtils.getString(rs, "name"));
                            r.setValue(ResultSetUtils.getDouble(rs, "price"));
                            return r;
                        }

                    });

                    assertEquals("[[romana,9.0], [margarita,7.0]]", r.toString());
                }
                {
                    List<Pair<String, Double>> lp = q.getExecutor().load( //
                            rs -> new Pair<String, Double>( //
                                    ResultSetUtils.getString(rs, "name"), //
                                    ResultSetUtils.getDouble(rs, "price") //
                            ));

                    assertEquals("[[romana,9.0], [margarita,7.0]]", lp.toString());
                }
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

}