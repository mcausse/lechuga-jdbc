package linenetcom;

import static org.junit.Assert.assertEquals;

import org.frijoles.jdbc.DataAccesFacade;
import org.frijoles.jdbc.JdbcDataAccesFacade;
import org.frijoles.mapper.HsqldbDDLGenerator;
import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Test;

import linenetcom.ent.Imputacio;
import linenetcom.ent.Project;
import linenetcom.ent.Tasca;
import linenetcom.ent.User;

public class LinenetTest {

    final DataAccesFacade facade;

    public LinenetTest() {
        JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:a");
        ds.setUser("sa");
        ds.setPassword("");
        this.facade = new JdbcDataAccesFacade(ds);
    }

    @Test
    public void testGenerateSqlScript() throws Exception {
        String sql = HsqldbDDLGenerator.generateScript(Project.class, Tasca.class, User.class, Imputacio.class);
        System.err.println(sql);
    }

    @Test
    public void testName() throws Exception {

        LinenetService s = new LinenetService(facade);
        s.panic();

        assertEquals(13.5, s.getHoresProjecte(100, null, null), 0.01);
        assertEquals(13.5, s.getHoresProjecte(100, "FASE1", null), 0.01);
        assertEquals(8.0, s.getHoresProjecte(100, null, 102), 0.01);

        assertEquals( //
                "[Imputacio [id=ImputacioId [idProject=100, idUser=102, dia=02/01/2014], desc=bbb, hores=4.5], " //
                        + "Imputacio [id=ImputacioId [idProject=100, idUser=102, dia=31/12/2013], desc=aaa, hores=3.5]]", //
                s.getUserImputations("mhc@linenet.com").toString());

    }

}
