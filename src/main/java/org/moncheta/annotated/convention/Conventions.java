package org.moncheta.annotated.convention;

public interface Conventions {

    String tableNameOf(Class<?> entityClass);

    String entityNameOf(String tableName);

    String columnNameOf(String propertyName);

    String propertyNameOf(String columnName);
}
