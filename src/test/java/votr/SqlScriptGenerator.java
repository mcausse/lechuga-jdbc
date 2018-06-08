package votr;

import org.frijoles.mapper.HsqldbDDLGenerator;

import votr.ent.Msg;
import votr.ent.Opcio;
import votr.ent.Usr;
import votr.ent.Votacio;

public class SqlScriptGenerator {

    public static void main(String[] args) {
        String sql = HsqldbDDLGenerator.generateScript(Votacio.class, Opcio.class, Usr.class, Msg.class);
        System.out.println(sql);
    }
}
