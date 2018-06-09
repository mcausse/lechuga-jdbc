package org.lechuga.annotated.convention;

public class DefaultConventions implements Conventions {

    @Override
    public String tableNameOf(Class<?> entityClass) {
        return camelCaseToSqlCase(entityClass.getSimpleName());
    }

    @Override
    public String columnNameOf(String propertyName) {
        return camelCaseToSqlCase(propertyName);
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

}
