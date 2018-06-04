package org.fijoles.mapper.autogen;

public class HsqldbSequence extends Generator {

    public HsqldbSequence(String sequenceName) {
        super("call next value for " + sequenceName);
    }

    @Override
    public boolean generateBefore() {
        return true;
    }

}
