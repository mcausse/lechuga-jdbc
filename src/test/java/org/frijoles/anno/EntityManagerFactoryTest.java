package org.frijoles.anno;

import static org.junit.Assert.assertEquals;

import org.frijoles.annotated.EntityManagerFactory;
import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.JdbcDataAccesFacade;
import org.frijoles.jdbc.util.SqlScriptExecutor;
import org.frijoles.mapper.EntityManager;
import org.frijoles.mapper.ents.ESex;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;

public class EntityManagerFactoryTest {

    DataAccesFacade facade;

    public EntityManagerFactoryTest() {
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