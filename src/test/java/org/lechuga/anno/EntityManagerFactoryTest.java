package org.lechuga.anno;

import static org.junit.Assert.assertEquals;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.anno.ents.Exp;
import org.lechuga.anno.ents.ExpId;
import org.lechuga.anno.ents.Fase;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.ents.ESex;

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

            {
                Exp e = new Exp(new ExpId(1982, null), "desc", "22/05/1982", ESex.FEMALE, new Fase(100, "FASE_INI"));
                em.store(e);

                Exp e2 = em.loadById(e.getId());
                assertEquals(e.toString(), e2.toString());
            }

            facade.commit();
        } catch (Throwable e) {
            facade.rollback();
            throw e;
        }
    }

}