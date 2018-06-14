package votr;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.util.Arrays;
import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.txproxy.TransactionalServiceProxyfier;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.HsqldbDDLGenerator;

import votr.VotrService.VotacioDto;
import votr.ent.Msg;
import votr.ent.Opcio;
import votr.ent.Usr;
import votr.ent.Votacio;

public class VotrTest {

    DataAccesFacade facade;

    public VotrTest() {
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
            sql.runFromClasspath("votr.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testGenerateSqlScript() throws Exception {
        String sql = HsqldbDDLGenerator.generateScript(Votacio.class, Opcio.class, Usr.class, Msg.class);
        System.err.println(sql);
    }

    @Test
    public void test() throws Exception {

        EntityManagerFactory emf = new EntityManagerFactory(facade, Votacio.class, Opcio.class, Usr.class, Msg.class);

        VotrService serv = TransactionalServiceProxyfier.proxyfy(facade, new VotrServiceImpl(emf), VotrService.class);

        Votacio v = new Votacio(null, "titol", "descripcio", null, null);
        List<Opcio> os = Arrays.asList( //
                new Opcio(null, "opcio1", "eneida"), //
                new Opcio(null, "opcio2", "odissea") //
        );
        List<Usr> us = Arrays.asList( //
                new Usr(null, "mhc@votr.com", null, null, null), //
                new Usr(null, "jbm@votr.com", null, null, null) //
        );

        serv.create(v, os, us);

        VotacioDto c = serv.carrega(v.getHashVotacio(), us.get(1).getHashUsr());

        assertEquals(2, c.os.size());
        assertEquals(2, c.usrs.size());

        assertNull(c.u.getAlias());
        assertNull(c.u.getNumOpcioVotada());

        serv.actualitzaUsrAlias(c.u.getHashVotacio(), c.u.getHashUsr(), "mhc");
        serv.vota(c.u.getHashVotacio(), c.u.getHashUsr(), 1);

        c = serv.carrega(v.getHashVotacio(), us.get(1).getHashUsr());

        assertEquals("mhc", c.u.getAlias());
        assertEquals(1, c.u.getNumOpcioVotada().intValue());

    }
}
