package org.frijoles.annotated;

import static org.junit.Assert.assertEquals;

import org.fijoles.annotated.EntityManagerFactory;
import org.fijoles.jdbc.DataAccesFacade;
import org.fijoles.jdbc.JdbcDataAccesFacade;
import org.fijoles.jdbc.util.SqlScriptExecutor;
import org.fijoles.mapper.EntityManager;
import org.frijoles.ents.ESex;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

public class A {

    DataAccesFacade facade;

    public A() {
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

        // Map<String, Field> fs = ReflectUtils.getFields(Exp.class);
        // for (Entry<String, Field> f : fs.entrySet()) {
        // System.out.println(f.getKey() + " => " + f.getValue());
        // }

        EntityManagerFactory emf = new EntityManagerFactory(facade);
        EntityManager<Exp, ExpId> em = emf.build(Exp.class, ExpId.class);

        facade.begin();
        try {
            Exp e = new Exp(new ExpId(1982, null), "desc", "22/05/1982", ESex.FEMALE, new Fase(100, "FASE_INI"));
            em.store(e);

            Exp e2 = em.loadById(e.getId());

            assertEquals(e.toString(), e2.toString());

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

}