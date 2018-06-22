package org.lechuga.annotated;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.lechuga.mapper.Column;
import org.lechuga.mapper.TableModel;
import org.lechuga.mapper.autogen.HsqldbIdentity;
import org.lechuga.mapper.autogen.HsqldbSequence;

public class HsqldbDDLGenerator {

    final TableModel<?> em;
    final Map<String, Column> idproperties;
    final Map<String, Column> properties;

    HsqldbDDLGenerator(TableModel<?> em) {
        super();
        this.em = em;
        this.idproperties = new LinkedHashMap<>();

        for (Column c : em.getIdColumns()) {
            idproperties.put(c.getPropertyName(), c);
        }

        this.properties = new LinkedHashMap<>(idproperties);
        for (Column c : em.getAllColumns()) {
            properties.put(c.getPropertyName(), c);
        }
    }

    public static String generateScript(Class<?>... metaClasses) {

        StringBuilder r = new StringBuilder();

        for (int i = metaClasses.length - 1; i >= 0; i--) {
            Class<?> c = metaClasses[i];
            IEntityManagerFactory emf = new EntityManagerFactory(null, c);
            TableModel<?> ec = emf.getModelByMetaClass(c);
            HsqldbDDLGenerator gen = new HsqldbDDLGenerator(ec);
            r.append(gen.generateDrops());
        }
        r.append("\n\n");
        for (Class<?> c : metaClasses) {
            IEntityManagerFactory emf = new EntityManagerFactory(null, c);
            TableModel<?> ec = emf.getModelByMetaClass(c);
            HsqldbDDLGenerator gen = new HsqldbDDLGenerator(ec);
            r.append(gen.generateCreates());
        }

        return r.toString();
    }

    protected String generateDrops() {
        StringBuilder r = new StringBuilder();
        r.append(generateDropTable());
        return r.toString();
    }

    protected String generateCreates() {
        StringBuilder r = new StringBuilder();
        r.append(generateCreateTable());
        r.append(generateAddPks());
        r.append(generateAutogenerated());
        return r.toString();
    }

    protected String generateAutogenerated() {
        StringBuilder r = new StringBuilder();
        for (Column p : properties.values()) {
            if (p.getGenerator() != null) {
                if (p.getGenerator() instanceof HsqldbIdentity) {
                    r.append(String.format(
                            "alter table %s alter column %s %s generated by default as identity(start with 100);%n",
                            em.getTableName(), p.getColumnName(), sqlTypeOf(p.getPropertyType())));
                } else if (p.getGenerator() instanceof HsqldbSequence) {

                    HsqldbSequence seq = (HsqldbSequence) p.getGenerator();
                    r.append("drop sequence " + seq.getSequenceName() + " if exists;\n");
                    r.append("create sequence " + seq.getSequenceName() + " start with 100;\n");

                } else {
                    r.append("-- TODO " + p.getGenerator().getClass().getName());
                }
            }
        }
        return r.toString();
    }

    protected String generateDropTable() {
        return "drop table " + em.getTableName() + " if exists;\n";
    }

    protected String generateCreateTable() {
        StringBuilder r = new StringBuilder();
        r.append("create table " + em.getTableName() + " (\n");
        int c = 0;
        for (Entry<String, Column> e : properties.entrySet()) {
            if (c > 0) {
                r.append(",\n");
            }
            c++;
            r.append(" ");
            r.append(e.getValue().getColumnName());
            r.append(" ");
            r.append(sqlTypeOf(e.getValue().getPropertyType()));

            r.append("\t/* " + e.getKey() + " */ ");
        }
        r.append("\n);\n");
        return r.toString();
    }

    protected String generateAddPks() {
        StringBuilder r = new StringBuilder();
        r.append("alter table " + em.getTableName());
        r.append(" add constraint pk_" + em.getTableName());
        r.append(" primary key (");

        int c = 0;
        for (Column e : idproperties.values()) {
            if (c > 0) {
                r.append(",");
            }
            c++;
            r.append(e.getColumnName());
        }

        r.append(");\n");
        return r.toString();
    }

    private static String sqlTypeOf(Class<?> type) {

        if (type.isPrimitive()) {
            if (type == int.class) {
                return "smallint";
            }
            if (type == long.class) {
                return "integer";
            }
            if (type == short.class) {
                return "tinyint";
            }
            if (type == byte.class) {
                return "tinyint";
            }
            if (type == float.class) {
                return "float";
            }
            if (type == double.class) {
                return "decimal";
            }
            if (type == boolean.class) {
                return "bit";
            }
            if (type == char.class) {
                return "varchar(1)";
            }
        } else if (Number.class.isAssignableFrom(type)) {
            if (type == Integer.class) {
                return "smallint";
            }
            if (type == Long.class) {
                return "integer";
            }
            if (type == Short.class) {
                return "tinyint";
            }
            if (type == Byte.class) {
                return "tinyint";
            }
            if (type == Float.class) {
                return "float";
            }
            if (type == Double.class) {
                return "decimal";
            }
            if (type == BigDecimal.class) {
                return "decimal";
            }
        } else if (type.isEnum()) {
            return "varchar(20)";
        } else {
            if (type == String.class) {
                return "varchar(100)";
            }
            if (type == Date.class) {
                return "timestamp";
            }
            if (type == BigDecimal.class) {
                return "decimal";
            }
            if (type == byte[].class) {
                return "binary(100)";
            }
            if (type == Boolean.class) {
                return "bit";
            }
            if (type == Character.class) {
                return "varchar(1)";
            }
        }

        throw new RuntimeException("unsupported column type: " + type.getName());
    }

}
