package org.fijoles.annotated.convention;

public interface Conventions {

    String tableNameOf(Class<?> entityClass);

    String columnNameOf(String propertyName);

}
