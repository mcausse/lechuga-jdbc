package examples.ex2.exps;

import static org.junit.Assert.assertEquals;

import java.util.List;

import org.hsqldb.jdbc.JDBCDataSource;
import org.junit.Before;
import org.junit.Test;
import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.annotated.HsqldbDDLGenerator;
import org.lechuga.annotated.IEntityManagerFactory;
import org.lechuga.annotated.ManyToOne;
import org.lechuga.annotated.MetaField;
import org.lechuga.annotated.OneToMany;
import org.lechuga.annotated.PropPair;
import org.lechuga.annotated.anno.Column;
import org.lechuga.annotated.anno.Entity;
import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.JdbcDataAccesFacade;
import org.lechuga.jdbc.util.SqlScriptExecutor;
import org.lechuga.mapper.Order;
import org.lechuga.mapper.autogen.HsqldbSequence;

public class ExpTest {

    final DataAccesFacade facade;

    public ExpTest() {
        final JDBCDataSource ds = new JDBCDataSource();
        ds.setUrl("jdbc:hsqldb:mem:a");
        ds.setUser("sa");
        ds.setPassword("");
        this.facade = new JdbcDataAccesFacade(ds);
    }

    @Before
    public void before() {
        facade.begin();
        try {
            SqlScriptExecutor sql = new SqlScriptExecutor(facade);
            sql.runFromClasspath("exptex.sql");
            facade.commit();
        } catch (Exception e) {
            facade.rollback();
            throw e;
        }
    }

    @Test
    public void testGen() throws Exception {

        String r = HsqldbDDLGenerator.generateScript(Tex_.class, Exp_.class);
        System.err.println(r);
    }

    @Test
    public void testName() throws Exception {

        IEntityManagerFactory emf = new EntityManagerFactory(facade, Exp_.class, Tex_.class);

        ExpId expId = new ExpId("08201", 1987, 123L);

        emf.getFacade().begin();
        try {

            Exp e = new Exp(expId, "old-exp");
            Tex t1 = new Tex(null, "fase1", e.getId().getCodPos(), e.getId().getAnyExp(), e.getId().getNumExp());
            Tex t2 = new Tex(null, "fase2", e.getId().getCodPos(), e.getId().getAnyExp(), e.getId().getNumExp());

            emf.getEntityManager(Exp.class).store(e);
            emf.getEntityManager(Tex.class).store(t1);
            emf.getEntityManager(Tex.class).store(t2);

            emf.getFacade().commit();
        } catch (Exception e) {
            emf.getFacade().rollback();
            throw new RuntimeException(e);
        }

        emf.getFacade().begin();
        try {

            Exp e = emf.getEntityManager(Exp.class).loadById(expId);
            List<Tex> texs = Exp_.texs.load(emf, e, Order.asc(Tex_.codPos), Order.asc(Tex_.anyExp), Order.asc(Tex_.numExp));

            Exp e0 = Tex_.exp.load(emf, texs.get(0));
            Exp e1 = Tex_.exp.load(emf, texs.get(1));

            assertEquals("Exp [id=ExpId [codPos=08201, anyExp=1987, numExp=123], desc=old-exp]", e.toString());
            assertEquals("Exp [id=ExpId [codPos=08201, anyExp=1987, numExp=123], desc=old-exp]", e0.toString());
            assertEquals("Exp [id=ExpId [codPos=08201, anyExp=1987, numExp=123], desc=old-exp]", e1.toString());

            assertEquals("[Tex [idTex=100, text=fase1, codPosx=08201, anyExpx=1987, numExpx=123], "
                    + "Tex [idTex=101, text=fase2, codPosx=08201, anyExpx=1987, numExpx=123]]", texs.toString());

        } finally {
            emf.getFacade().rollback();
        }

    }

    @Entity(entity = Exp.class)
    public static interface Exp_ {

        @Id
        public static final MetaField<Exp, String> codPos = new MetaField<>("id.codPos");
        @Id
        public static final MetaField<Exp, Integer> anyExp = new MetaField<>("id.anyExp");
        @Id
        public static final MetaField<Exp, Long> numExp = new MetaField<>("id.numExp");

        @Column("descr")
        public static final MetaField<Exp, String> desc = new MetaField<>("desc");

        public static final OneToMany<Exp, Tex> texs = new OneToMany<>(//
                Exp.class, Tex.class, //
                new PropPair<>(Exp_.codPos, Tex_.codPos), //
                new PropPair<>(Exp_.anyExp, Tex_.anyExp), //
                new PropPair<>(Exp_.numExp, Tex_.numExp) //
        );
    }

    public static class ExpId {

        String codPos;
        Integer anyExp;
        Long numExp;

        public ExpId() {
            super();
        }

        public ExpId(String codPos, Integer anyExp, Long numExp) {
            super();
            this.codPos = codPos;
            this.anyExp = anyExp;
            this.numExp = numExp;
        }

        public String getCodPos() {
            return codPos;
        }

        public void setCodPos(String codPos) {
            this.codPos = codPos;
        }

        public Integer getAnyExp() {
            return anyExp;
        }

        public void setAnyExp(Integer anyExp) {
            this.anyExp = anyExp;
        }

        public Long getNumExp() {
            return numExp;
        }

        public void setNumExp(Long numExp) {
            this.numExp = numExp;
        }

        @Override
        public String toString() {
            return "ExpId [codPos=" + codPos + ", anyExp=" + anyExp + ", numExp=" + numExp + "]";
        }

    }

    public static class Exp {

        ExpId id;
        String desc;

        public Exp() {
            super();
        }

        public Exp(ExpId id, String desc) {
            super();
            this.id = id;
            this.desc = desc;
        }

        public ExpId getId() {
            return id;
        }

        public void setId(ExpId id) {
            this.id = id;
        }

        public String getDesc() {
            return desc;
        }

        public void setDesc(String desc) {
            this.desc = desc;
        }

        @Override
        public String toString() {
            return "Exp [id=" + id + ", desc=" + desc + "]";
        }

    }

    @Entity(entity = Tex.class)
    public static interface Tex_ {

        @Id
        @Generated(value = HsqldbSequence.class, args = "seq_tex")
        public static final MetaField<Tex, Long> idTex = new MetaField<>("idTex");

        @Column("textx")
        public static final MetaField<Tex, String> text = new MetaField<>("text");

        public static final MetaField<Tex, String> codPos = new MetaField<>("codPosx");
        public static final MetaField<Tex, Integer> anyExp = new MetaField<>("anyExpx");
        public static final MetaField<Tex, Long> numExp = new MetaField<>("numExpx");

        public static final ManyToOne<Tex, Exp> exp = new ManyToOne<>(//
                Tex.class, Exp.class, //
                new PropPair<>(Tex_.codPos, Exp_.codPos), //
                new PropPair<>(Tex_.anyExp, Exp_.anyExp), //
                new PropPair<>(Tex_.numExp, Exp_.numExp) //
        );
    }

    public static class Tex {

        Long idTex;
        String text;

        String codPosx;
        Integer anyExpx;
        Long numExpx;

        public Tex() {
            super();
        }

        public Tex(Long idTex, String text, String codPosx, Integer anyExpx, Long numExpx) {
            super();
            this.idTex = idTex;
            this.text = text;
            this.codPosx = codPosx;
            this.anyExpx = anyExpx;
            this.numExpx = numExpx;
        }

        public Long getIdTex() {
            return idTex;
        }

        public void setIdTex(Long idTex) {
            this.idTex = idTex;
        }

        public String getText() {
            return text;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getCodPosx() {
            return codPosx;
        }

        public void setCodPosx(String codPosx) {
            this.codPosx = codPosx;
        }

        public Integer getAnyExpx() {
            return anyExpx;
        }

        public void setAnyExpx(Integer anyExpx) {
            this.anyExpx = anyExpx;
        }

        public Long getNumExpx() {
            return numExpx;
        }

        public void setNumExpx(Long numExpx) {
            this.numExpx = numExpx;
        }

        @Override
        public String toString() {
            return "Tex [idTex=" + idTex + ", text=" + text + ", codPosx=" + codPosx + ", anyExpx=" + anyExpx + ", numExpx=" + numExpx
                    + "]";
        }

    }

}
