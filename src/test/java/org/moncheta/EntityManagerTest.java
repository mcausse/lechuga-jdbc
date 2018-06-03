package org.moncheta;

import static org.junit.Assert.assertEquals;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.moncheta.ents.Apoyo;
import org.moncheta.ents.Dog;
import org.moncheta.ents.ESex;
import org.moncheta.ents.IdDog;
import org.moncheta.ents.Pizza;
import org.moncheta.jdbc.DataAccesFacade;
import org.moncheta.jdbc.JdbcDataAccesFacade;
import org.moncheta.jdbc.util.SqlScriptExecutor;
import org.moncheta.mapper.Accessor;
import org.moncheta.mapper.Column;
import org.moncheta.mapper.EntityManager;
import org.moncheta.mapper.TableModel;
import org.moncheta.mapper.autogen.HsqldbIdentity;
import org.moncheta.mapper.autogen.HsqldbSequence;
import org.moncheta.mapper.handler.EnumeratedHandler;
import org.moncheta.mapper.handler.Handlers;

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

            em.delete(chucho);
            em.deleteById(faria.getId());
            assertEquals("[]", em.loadAll().toString());

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
