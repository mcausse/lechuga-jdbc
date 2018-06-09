package org.lechuga.mapper.autogen;

public class HsqldbSequence extends Generator {

    final String sequenceName;

    public HsqldbSequence(String sequenceName) {
        super("call next value for " + sequenceName);
        this.sequenceName = sequenceName;
    }

    @Override
    public boolean generateBefore() {
        return true;
    }

    public String getSequenceName() {
        return sequenceName;
    }

}
