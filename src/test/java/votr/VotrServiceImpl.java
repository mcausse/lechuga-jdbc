package votr;

import java.util.Date;
import java.util.List;

import org.lechuga.annotated.EntityManagerFactory;
import org.lechuga.jdbc.DataAccesFacade;
import org.lechuga.jdbc.txproxy.TransactionalMethod;
import org.lechuga.mapper.EntityManager;
import org.lechuga.mapper.Order;

import votr.ent.Msg;
import votr.ent.Opcio;
import votr.ent.OpcioId;
import votr.ent.Usr;
import votr.ent.Votacio;

public class VotrServiceImpl implements VotrService {

    final DataAccesFacade daf;

    final EntityManager<Votacio, String> votacionsDao;
    final EntityManager<Opcio, OpcioId> opcionsDao;
    final EntityManager<Usr, String> usersDao;
    final EntityManager<Msg, Integer> msgsDao;

    public VotrServiceImpl(DataAccesFacade daf) {
        super();
        this.daf = daf;
        EntityManagerFactory emf = new EntityManagerFactory(daf);
        this.votacionsDao = emf.build(Votacio.class, String.class);
        this.opcionsDao = emf.build(Opcio.class, OpcioId.class);
        this.usersDao = emf.build(Usr.class, String.class);
        this.msgsDao = emf.build(Msg.class, Integer.class);
    }

    protected String calcHash(Object... os) {
        int i = 0;
        for (Object o : os) {
            i += o.hashCode();
        }
        return String.valueOf(Integer.toHexString(i));
    }

    @TransactionalMethod
    @Override
    public void create(Votacio v, List<Opcio> os, List<Usr> usrs) {

        v.setDataCreacio(new Date());
        String hashVotacio = calcHash(v.getTitol(), v.getDescripcio(), v.getDataCreacio().hashCode());
        v.setHashVotacio(hashVotacio);
        votacionsDao.insert(v);

        for (Usr u : usrs) {
            String hashUsr = calcHash(v.getHashVotacio(), u.getEmail());
            u.setHashUsr(hashUsr);
            u.setAlias(null);
            u.setNumOpcioVotada(null);
            u.setHashVotacio(hashVotacio);
            usersDao.insert(u);
        }

        int i = 0;
        for (Opcio o : os) {
            o.setIdOpcio(new OpcioId(hashVotacio, i));
            opcionsDao.insert(o);
            i++;
        }
    }

    @TransactionalMethod(readOnly = true)
    @Override
    public VotacioDto carrega(String hashVotacio, String hashUsuari) {
        Votacio v = votacionsDao.loadById(hashVotacio);
        List<Opcio> os = opcionsDao.loadByProp("idOpcio.hashVotacio", hashVotacio, Order.asc("idOpcio.num"));
        List<Usr> us = usersDao.loadByProp("hashVotacio", hashVotacio);
        Usr u = usersDao.loadById(hashUsuari);
        if (!u.getHashVotacio().equals(hashVotacio)) {
            throw new RuntimeException();
        }
        return new VotacioDto(v, u, os, us);
    }

    @TransactionalMethod
    @Override
    public void actualitzaUsrAlias(String hashVotacio, String hashUsuari, String alias) {
        Usr u = usersDao.loadById(hashUsuari);
        if (!u.getHashVotacio().equals(hashVotacio)) {
            throw new RuntimeException();
        }
        u.setAlias(alias);
        usersDao.update(u, "alias");
    }

    @TransactionalMethod
    @Override
    public void vota(String hashVotacio, String hashUsuari, int numOpcioVotada) {
        Usr u = usersDao.loadById(hashUsuari);
        if (!u.getHashVotacio().equals(hashVotacio)) {
            throw new RuntimeException();
        }
        u.setNumOpcioVotada(numOpcioVotada);
        usersDao.update(u, "numOpcioVotada");
    }

}
