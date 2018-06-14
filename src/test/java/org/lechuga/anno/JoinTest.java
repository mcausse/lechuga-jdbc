package org.lechuga.anno;

import static org.junit.Assert.assertEquals;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.anno.ents.Ingredient;
import org.lechuga.anno.ents.Pizza;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.criteria.CriteriaBuilder;
import org.lechuga.annotated.criteria.ELike;
import org.lechuga.annotated.criteria.Restrictions;
import org.lechuga.annotated.query.QueryBuilder;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.ResultSetUtils;
import org.lechuga.jdbc.RowMapper;
import org.lechuga.jdbc.extractor.MapResultSetExtractor;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.util.Pair;

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
    public void testCriteria() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Pizza.class, Ingredient.class);
        EntityManager<Pizza, Long> pem = emf.buildEntityManager(Pizza.class);
        EntityManager<Ingredient, Integer> iem = emf.buildEntityManager(Ingredient.class);

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

                Restrictions pr = emf.getRestrictions(Pizza.class, "p");
                Restrictions ir = emf.getRestrictions(Ingredient.class, "i");

                CriteriaBuilder c = emf.createCriteria();
                c.append("select {}, sum({}) as price ", pr.column("name"), ir.column("price"));
                c.append("from {} join {} ", pr.table(), ir.table());
                c.append("on {} ", pr.eq("id", ir, "idPizza"));
                c.append("where {} ", ir.gt("price", 0.0));
                c.append("group by {} ", pr.column("name"));

                assertEquals( //
                        "select p.name, sum(i.price) as price " + //
                                "from pizzas p join ingredients i on p.id=i.id_pizza " + //
                                "where i.price>? group by p.name  -- [0.0(Double)]", //
                        c.toString());

                {
                    List<Map<String, Object>> r = c.extract(new MapResultSetExtractor());
                    assertEquals("[{NAME=romana, PRICE=9.0}, {NAME=margarita, PRICE=7.0}]", r.toString());
                }
                {
                    RowMapper<Pair<String, Double>> rm = new RowMapper<Pair<String, Double>>() {

                        @Override
                        public Pair<String, Double> mapRow(ResultSet rs) throws SQLException {
                            Pair<String, Double> r = new Pair<>();
                            r.setKey(ResultSetUtils.getString(rs, "name"));
                            r.setValue(ResultSetUtils.getDouble(rs, "price"));
                            return r;
                        }

                    };
                    List<Pair<String, Double>> r = c.getExecutor(rm).load();

                    assertEquals("[[romana,9.0], [margarita,7.0]]", r.toString());
                }
                {

                    RowMapper<Pair<String, Double>> rm = rs -> new Pair<String, Double>( //
                            ResultSetUtils.getString(rs, "name"), //
                            ResultSetUtils.getDouble(rs, "price") //
                    );
                    List<Pair<String, Double>> lp = c.getExecutor(rm).load();

                    assertEquals("[[romana,9.0], [margarita,7.0]]", lp.toString());
                }
            }

            {
                Restrictions pr = emf.getRestrictions(Pizza.class, "p");

                CriteriaBuilder c = emf.createCriteria();
                c.append("select {} ", pr.all());
                c.append("from {} ", pr.table());
                c.append("where {} ", //
                        Restrictions.and( //
                                pr.ilike("name", ELike.CONTAINS, "man"), //
                                pr.between("id", 100L, 200L), pr.in("id", 100L, 101L, 102L)//
                        ) //
                );

                assertEquals( //
                        "select p.id,p.name from pizzas p where upper(p.name) like upper(?) and p.id between ? and ? and p.id in (?,?,?)  -- [%man%(String), 100(Long), 200(Long), 100(Long), 101(Long), 102(Long)]", //
                        c.toString());

                c.getExecutor(pem).load();
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testComposition() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Pizza.class, Ingredient.class);
        EntityManager<Pizza, Long> pem = emf.buildEntityManager(Pizza.class);
        EntityManager<Ingredient, Integer> iem = emf.buildEntityManager(Ingredient.class);

        facade.begin();
        try {

            Pizza romana = new Pizza(null, "romana");
            pem.store(romana);
            iem.store(new Ingredient(null, "base", 7.0, romana.getId()));
            iem.store(new Ingredient(null, "olivanchoa", 2.0, romana.getId()));

            QueryBuilder<Ingredient> q = emf.createQuery(Ingredient.class, "i");
            q.append("select {i.*} from {i} where {i.idPizza=?}", romana.getId());
            List<Ingredient> is = q.getExecutor().load();
            assertEquals(
                    "[Ingredient [id=100, name=base, price=7.0, idPizza=100], Ingredient [id=101, name=olivanchoa, price=2.0, idPizza=100]]",
                    is.toString());

            QueryBuilder<Pizza> q2 = emf.createQuery(Pizza.class, "p");
            q2.addEm("i", Ingredient.class);
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

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Pizza.class, Ingredient.class);
        EntityManager<Pizza, Long> pem = emf.buildEntityManager(Pizza.class);
        EntityManager<Ingredient, Integer> iem = emf.buildEntityManager(Ingredient.class);

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

                QueryBuilder<Pizza> q = emf.createQuery(Pizza.class, "p");
                q.addEm("i", Ingredient.class);
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