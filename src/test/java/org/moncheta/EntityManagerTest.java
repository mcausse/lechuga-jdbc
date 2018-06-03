package org.moncheta;

import static org.junit.Assert.assertEquals;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.moncheta.jdbc.DataAccesFacade;
import org.moncheta.jdbc.JdbcDataAccesFacade;
import org.moncheta.jdbc.util.SqlScriptExecutor;
import org.moncheta.mapper.Accessor;
import org.moncheta.mapper.Column;
import org.moncheta.mapper.EntityManager;
import org.moncheta.mapper.TableModel;
import org.moncheta.mapper.autogen.HsqldbIdentity;
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
    public void testName() throws Exception {

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
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }

    }

    public static enum ESex {
        MALE, FEMALE;
    }

    public static class IdDog {
        Long idDog;
        String name;

        public IdDog() {
            super();
        }

        public IdDog(Long idDog, String name) {
            super();
            this.idDog = idDog;
            this.name = name;
        }

        public Long getIdDog() {
            return idDog;
        }

        public void setIdDog(Long idDog) {
            this.idDog = idDog;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        @Override
        public String toString() {
            return "IdDog [idDog=" + idDog + ", name=" + name + "]";
        }

    }

    public static class Dog {

        IdDog id;
        int age;
        ESex sex;
        boolean dead;

        public Dog() {
            super();
        }

        public Dog(IdDog id, int age, ESex sex, boolean dead) {
            super();
            this.id = id;
            this.age = age;
            this.sex = sex;
            this.dead = dead;
        }

        public IdDog getId() {
            return id;
        }

        public void setId(IdDog id) {
            this.id = id;
        }

        public int getAge() {
            return age;
        }

        public void setAge(int age) {
            this.age = age;
        }

        public ESex getSex() {
            return sex;
        }

        public void setSex(ESex sex) {
            this.sex = sex;
        }

        public boolean isDead() {
            return dead;
        }

        public void setDead(boolean dead) {
            this.dead = dead;
        }

        @Override
        public String toString() {
            return "Dog [id=" + id + ", age=" + age + ", sex=" + sex + ", dead=" + dead + "]";
        }

    }
}
