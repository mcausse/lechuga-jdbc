package org.frijoles.annotated.convention;

public interface Conventions {

    String tableNameOf(Class<?> entityClass);

    String columnNameOf(String propertyName);

}
