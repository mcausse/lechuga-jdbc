package query2;

import static org.junit.Assert.assertEquals;

import java.util.StringJoiner;

import org.frijoles.anno.ents.Exp;
import org.frijoles.anno.ents.ExpId;
import org.frijoles.anno.ents.Fase;
import org.frijoles.annotated.EntityManagerFactory;
import org.frijoles.jdbc.queryobject.Query;
import org.frijoles.mapper.Column;
import org.frijoles.mapper.EntityManager;
import org.frijoles.mapper.TableModel;
import org.frijoles.mapper.TableModel.Q;
import org.junit.Test;

/**
 * fer Criteria amb objectes compostos => algo que amb QueryBuilder no sé com
 * fer.
 * <p>
 * El Criteria té el problema de l'aliasing opcional.
 */
public class Criterianeida {

    @Test
    public void testName() throws Exception {
        EntityManagerFactory emf = new EntityManagerFactory(null);
        EntityManager<Exp, ExpId> em = emf.build(Exp.class, ExpId.class);

        {
            CriteriaRoot<Exp> c = new CriteriaRoot<Exp>(em.getModel());

            assertEquals("codi_fase=? and id_fase=? -- [INI(String), 100(Integer)]",
                    c.get("fase").eq(new Fase(100, "INI")).toString());

            assertEquals("data_ini=? -- [Sat May 22 00:00:00 CEST 1982(Date)]",
                    c.get("dataIni").eq("22/05/1982").toString());
        }
        {
            CriteriaRoot<Exp> c = new CriteriaRoot<Exp>(em.getModel(), "f");

            assertEquals("f.codi_fase=? and f.id_fase=? -- [INI(String), 100(Integer)]",
                    c.get("fase").eq(new Fase(100, "INI")).toString());

            assertEquals("f.data_ini=? -- [Sat May 22 00:00:00 CEST 1982(Date)]",
                    c.get("dataIni").eq("22/05/1982").toString());
        }

        {
            CriteriaRoot<Exp> c = new CriteriaRoot<Exp>(em.getModel());
            CriteriaRoot<Exp> c2 = new CriteriaRoot<Exp>(em.getModel());
            assertEquals("codi_fase=codi_fase and id_fase=id_fase -- []", c.get("fase").eq(c2.get("fase")).toString());
        }
        {
            CriteriaRoot<Exp> c = new CriteriaRoot<Exp>(em.getModel(), "f");
            CriteriaRoot<Exp> c2 = new CriteriaRoot<Exp>(em.getModel(), "k");
            assertEquals("f.codi_fase=k.codi_fase and f.id_fase=k.id_fase -- []",
                    c.get("fase").eq(c2.get("fase")).toString());
        }
    }

    public static class Criterion extends Query {

    }

    public static class CriteriaRoot<E> {

        final TableModel<E> model;
        final String alias;

        public CriteriaRoot(TableModel<E> model, String alias) {
            super();
            this.model = model;
            this.alias = alias;
        }

        public CriteriaRoot(TableModel<E> model) {
            this(model, null);
        }

        public Prop get(String propertyName) {
            return new Prop(alias, model.findColumnKKKByName(propertyName));
        }
    }

    public static class Prop {

        final String alias;
        final Q cs;

        public Prop(String alias, Q cs) {
            super();
            this.alias = alias;
            this.cs = cs;
        }

        public Criterion propValue(String join, String op, Object value) {
            Criterion q = new Criterion();
            StringJoiner j = new StringJoiner(join);
            for (Column c : cs.getCs()) {
                if (alias == null) {
                    j.add(c.getColumnName() + op + "?");
                } else {
                    j.add(alias + "." + c.getColumnName() + op + "?");
                }
                if (cs.getOffset() < 0) {
                    q.addArg(c.convertValueForJdbc(value));
                } else {
                    q.addArg(c.getValueForJdbc(value, cs.getOffset()));
                }
            }
            q.append(j.toString());
            return q;
        }

        public Criterion eq(Object value) {
            return propValue(" and ", "=", value);
        }

        public Criterion propProp(String join, String op, Prop prop) {
            Criterion q = new Criterion();
            StringJoiner j = new StringJoiner(join);
            for (Column c1 : cs.getCs()) {
                for (Column c2 : prop.cs.getCs()) {
                    if (!c1.getColumnName().equals(c2.getColumnName())) {
                        continue;
                    }

                    String cc1;
                    if (alias == null) {
                        cc1 = c1.getColumnName();
                    } else {
                        cc1 = alias + "." + c1.getColumnName();
                    }
                    String cc2;
                    if (prop.alias == null) {
                        cc2 = c2.getColumnName();
                    } else {
                        cc2 = prop.alias + "." + c2.getColumnName();
                    }
                    j.add(cc1 + op + cc2);
                }
            }
            q.append(j.toString());
            return q;
        }

        public Criterion eq(Prop prop) {
            return propProp(" and ", "=", prop);
        }

    }
}
