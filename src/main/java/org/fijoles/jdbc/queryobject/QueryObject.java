package org.fijoles.jdbc.queryobject;

import java.util.Collection;

public interface QueryObject {

    String getSql();

    Object[] getArgs();

    Collection<Object> getArgsList();

}