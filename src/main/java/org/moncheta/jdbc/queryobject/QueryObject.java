package org.moncheta.jdbc.queryobject;

public interface QueryObject {

    String getSql();

    Object[] getArgs();

}