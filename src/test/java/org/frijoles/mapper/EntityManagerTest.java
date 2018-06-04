package org.frijoles.mapper;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.JdbcDataAccesFacade;
import org.frijoles.jdbc.ScalarMappers;
import org.frijoles.jdbc.extractor.MapResultSetExtractor;
import org.frijoles.jdbc.extractor.Pager;
import org.frijoles.jdbc.queryobject.Query;
import org.frijoles.jdbc.util.SqlScriptExecutor;
import org.frijoles.mapper.autogen.HsqldbIdentity;
import org.frijoles.mapper.autogen.HsqldbSequence;
import org.frijoles.mapper.ents.Apoyo;
import org.frijoles.mapper.ents.Dog;
import org.frijoles.mapper.ents.ESex;
import org.frijoles.mapper.ents.IdDog;
import org.frijoles.mapper.ents.Pizza;
import org.frijoles.mapper.handler.EnumeratedHandler;
import org.frijoles.mapper.handler.Handlers;
import org.frijoles.mapper.query.Executor;
import org.frijoles.mapper.query.QueryBuilder;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

public class EntityManagerTest {

    DataAccesFacade facade;

    public EntityManagerTest() {
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
            sql.runFromClasspath("test.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testPizzas() throws Exception {

        TableModel<Pizza> p = new TableModel<>(Pizza.class, "pizzas");
        p.addColumn(new Column(true, "id", new Accessor(Pizza.class, "id"), Handlers.LONG,
                new HsqldbSequence("seq_pizza")));
        p.addColumn(new Column(false, "name", new Accessor(Pizza.class, "name"), Handlers.STRING, null));
        p.addColumn(new Column(false, "price", new Accessor(Pizza.class, "price"), Handlers.DOUBLE, null));

        EntityManager<Pizza, Long> em = new EntityManager<>(facade, p);

        facade.begin();
        try {

            assertEquals("[]", em.loadAll().toString());

            Pizza romana = new Pizza(null, "romana", 12.5);
            em.store(romana);
            assertEquals("[Pizza [id=100, name=romana, price=12.5]]", em.loadAll().toString());

            romana.setPrice(11.2);
            em.store(romana);
            assertEquals("Pizza [id=100, name=romana, price=11.2]", em.loadById(100L).toString());

            //

            Pizza napolitana = new Pizza(null, "napolitana", 9.5);
            em.store(napolitana);
            assertEquals("[Pizza [id=100, name=romana, price=11.2], Pizza [id=101, name=napolitana, price=9.5]]",
                    em.loadAll().toString());

            //

            assertEquals("[Pizza [id=100, name=romana, price=11.2]]", em.loadByProp("price", "11.2").toString());
            assertEquals("Pizza [id=101, name=napolitana, price=9.5]", em.loadUniqueByProp("price", "9.5").toString());

            //

            assertTrue(em.exists(romana));
            assertTrue(em.existsById(romana.getId()));
            assertFalse(em.exists(new Pizza(616L, null, 0.0)));
            assertFalse(em.existsById(616L));

            em.delete(romana);
            em.deleteById(napolitana.getId());
            assertEquals("[]", em.loadAll().toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

    }

    @Test
    public void testDogs() throws Exception {

        TableModel<Dog> p = new TableModel<>(Dog.class, "dogs");
        p.addColumn(
                new Column(true, "id_dog", new Accessor(Dog.class, "id.idDog"), Handlers.LONG, new HsqldbIdentity()));
        p.addColumn(new Column(true, "name", new Accessor(Dog.class, "id.name"), Handlers.STRING, null));
        p.addColumn(new Column(false, "age", new Accessor(Dog.class, "age"), Handlers.PINTEGER, null));
        p.addColumn(new Column(false, "sex", new Accessor(Dog.class, "sex"), new EnumeratedHandler(ESex.class), null));
        p.addColumn(new Column(false, "dead", new Accessor(Dog.class, "dead"), Handlers.BOOLEAN, null));

        EntityManager<Dog, IdDog> em = new EntityManager<>(facade, p);

        facade.begin();
        try {

            assertEquals("[]", em.loadAll().toString());

            Dog chucho = new Dog(new IdDog(null, "chucho"), 9, ESex.MALE, false);
            em.store(chucho);
            assertEquals("[Dog [id=IdDog [idDog=100, name=chucho], age=9, sex=MALE, dead=false]]",
                    em.loadAll().toString());

            chucho.setSex(ESex.FEMALE);
            em.store(chucho);
            assertEquals("Dog [id=IdDog [idDog=100, name=chucho], age=9, sex=FEMALE, dead=false]",
                    em.loadById(new IdDog(100L, "chucho")).toString());

            //

            Dog faria = new Dog(new IdDog(null, "faria"), 11, ESex.FEMALE, true);
            em.store(faria);
            assertEquals(
                    "[Dog [id=IdDog [idDog=100, name=chucho], age=9, sex=FEMALE, dead=false], Dog [id=IdDog [idDog=101, name=faria], age=11, sex=FEMALE, dead=true]]",
                    em.loadAll().toString());

            //

            assertEquals("[Dog [id=IdDog [idDog=100, name=chucho], age=9, sex=FEMALE, dead=false]]",
                    em.loadByProp("id.name", "chucho").toString());
            assertEquals("Dog [id=IdDog [idDog=100, name=chucho], age=9, sex=FEMALE, dead=false]",
                    em.loadUniqueByProp("id.name", "chucho").toString());

            //

            QueryBuilder<Dog> q = em.createQuery("d");
            q.append("select {d.*} from {d} where ");
            q.append("{d.age>?} and {d.age<?} ", 1L, 99L);
            q.append("and {d.sex in (?,?)}", ESex.FEMALE, ESex.MALE);
            List<Dog> dd = q.getExecutor().load();
            assertEquals(
                    "[Dog [id=IdDog [idDog=100, name=chucho], age=9, sex=FEMALE, dead=false], Dog [id=IdDog [idDog=101, name=faria], age=11, sex=FEMALE, dead=true]]",
                    dd.toString());

            //

            em.delete(chucho);
            em.deleteById(faria.getId());
            assertEquals("[]", em.loadAll().toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

        facade.begin();
        try {

            assertEquals("[]", em.loadAll().toString());

            for (int i = 0; i < 9; i++) {
                Dog d = new Dog(new IdDog(null, "chucho" + i), 9 + i, ESex.MALE, false);
                em.store(d);
            }

            QueryBuilder<Dog> q = em.createQuery("d");
            q.append("select {d.*} from {d} order by {d.id.name} asc");
            Executor<Dog> ex = q.getExecutor();

            Pager<Dog> p1 = ex.loadPage(5, 0);
            Pager<Dog> p2 = ex.loadPage(5, 1);
            Pager<Dog> p3 = ex.loadPage(5, 2);

            assertEquals(
                    "Pager [pageSize=5, numPage=0, totalRows=9, totalPages=2, page=[Dog [id=IdDog [idDog=102, name=chucho0], age=9, sex=MALE, dead=false], Dog [id=IdDog [idDog=103, name=chucho1], age=10, sex=MALE, dead=false], Dog [id=IdDog [idDog=104, name=chucho2], age=11, sex=MALE, dead=false], Dog [id=IdDog [idDog=105, name=chucho3], age=12, sex=MALE, dead=false], Dog [id=IdDog [idDog=106, name=chucho4], age=13, sex=MALE, dead=false]]]",
                    p1.toString());
            assertEquals(
                    "Pager [pageSize=5, numPage=1, totalRows=9, totalPages=2, page=[Dog [id=IdDog [idDog=107, name=chucho5], age=14, sex=MALE, dead=false], Dog [id=IdDog [idDog=108, name=chucho6], age=15, sex=MALE, dead=false], Dog [id=IdDog [idDog=109, name=chucho7], age=16, sex=MALE, dead=false], Dog [id=IdDog [idDog=110, name=chucho8], age=17, sex=MALE, dead=false]]]",
                    p2.toString());
            assertEquals("Pager [pageSize=5, numPage=2, totalRows=9, totalPages=2, page=[]]", p3.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

        facade.begin();
        try {

            facade.update(Query.immutable("delete from " + p.getTableName()));

            assertEquals("[]", em.loadAll().toString());

            Dog d = new Dog(new IdDog(null, "chucho"), 9, ESex.MALE, false);
            em.store(d);

            QueryBuilder<Dog> q = em.createQuery("d");
            q.append("select {d.*} from {d} order by {d.id.name} asc");
            Executor<Dog> ex = q.getExecutor();

            List<Map<String, Object>> r = ex.extract(new MapResultSetExtractor());
            assertEquals("[{ID_DOG=111, NAME=chucho, AGE=9, SEX=MALE, DEAD=false}]", r.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

        facade.begin();
        try {

            {
                QueryBuilder<Dog> q = em.createQuery("t");
                q.append("delete from {t} where {t.id.idDog is not null}");
                int r = q.getExecutor().update();
                assertEquals(1, r);
            }
            {
                QueryBuilder<Dog> q = em.createQuery("t");
                q.append("select count(*) from {t}");
                long count = q.getExecutor().loadUnique(ScalarMappers.LONG);
                assertEquals(0, count);
            }
            {
                Dog d = new Dog(new IdDog(null, "chucho"), 9, ESex.MALE, false);
                em.store(d);
            }
            {
                QueryBuilder<Dog> q = em.createQuery("t");
                q.append("select count(*) from {t}");
                long count = q.getExecutor().loadUnique(ScalarMappers.LONG);
                assertEquals(1, count);
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testApoyo() throws Exception {

        TableModel<Apoyo> p = new TableModel<>(Apoyo.class, "apoyo");
        p.addColumn(new Column(true, "key", new Accessor(Apoyo.class, "key"), Handlers.STRING, null));
        p.addColumn(new Column(false, "value", new Accessor(Apoyo.class, "value"), Handlers.STRING, null));

        EntityManager<Apoyo, String> em = new EntityManager<>(facade, p);

        facade.begin();
        try {

            assertEquals("[]", em.loadAll().toString());

            Apoyo one = new Apoyo("one", "1");
            Apoyo two = new Apoyo("two", "20");
            Apoyo three = new Apoyo("three", "3");

            em.store(one);
            em.store(two);
            em.store(three);
            assertEquals("[Apoyo [key=one, value=1], Apoyo [key=three, value=3], Apoyo [key=two, value=20]]",
                    em.loadAll().toString());

            two.setValue("2");
            em.store(two);
            assertEquals("Apoyo [key=two, value=2]", em.loadById("two").toString());

            //

            em.delete(one);
            em.delete(two);
            em.deleteById(three.getKey());
            assertEquals("[]", em.loadAll().toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }

    }
}
