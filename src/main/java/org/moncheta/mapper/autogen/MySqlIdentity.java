package org.moncheta.mapper.autogen;

public class MySqlIdentity extends Generator {

    public MySqlIdentity() {
        super("select last_insert_id()");
    }

    @Override
    public boolean generateBefore() {
        return false;
    }

}
