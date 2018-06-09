package org.lechuga.mapper.autogen;

public class OracleSequence extends Generator {

    public OracleSequence(String sequenceName) {
        super("select " + sequenceName + ".nextval from dual");
    }

    @Override
    public boolean generateBefore() {
        return true;
    }

}
