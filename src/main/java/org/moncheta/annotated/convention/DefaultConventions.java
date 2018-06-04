package org.moncheta.annotated.convention;

public class DefaultConventions implements Conventions {

    @Override
    public String tableNameOf(Class<?> entityClass) {
        return camelCaseToSqlCase(entityClass.getSimpleName());
    }

    @Override
    public String entityNameOf(String tableName) {
        return sqlCaseToUpperCamelCase(tableName);
    }

    @Override
    public String columnNameOf(String propertyName) {
        return camelCaseToSqlCase(propertyName);
    }

    @Override
    public String propertyNameOf(String columnName) {
        return sqlCaseToCamelCase(columnName);
    }

    // això va mooolt més ràpid que fent-ho amb regexp
    protected String camelCaseToSqlCase(final String s) {
        final StringBuffer strb = new StringBuffer(s.length() + 5);
        strb.append(s.charAt(0));
        for (int i = 1; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (Character.isUpperCase(c)) {
                strb.append('_');
            }
            strb.append(c);
        }
        return strb.toString().toLowerCase();
    }

    protected String sqlCaseToCamelCase(final String s) {
        final StringBuilder r = new StringBuilder();

        for (int i = 0; i < s.length(); i++) {
            final char c = s.charAt(i);
            if (c == '_') {
                while (i < s.length() && s.charAt(i) == '_') {
                    i++;
                }
                if (i < s.length()) {
                    r.append(Character.toUpperCase(s.charAt(i)));
                }
            } else {
                r.append(Character.toLowerCase(c));
            }
        }
        return r.toString();
    }

    protected String sqlCaseToUpperCamelCase(final String s) {
        String s2 = sqlCaseToCamelCase(s);
        return Character.toUpperCase(s2.charAt(0)) + s2.substring(1);
    }

}
