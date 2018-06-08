package votr;

import java.util.List;

import votr.ent.Opcio;
import votr.ent.Usr;
import votr.ent.Votacio;

public interface VotrService {

    void create(Votacio v, List<Opcio> os, List<Usr> usrs);

    VotacioDto carrega(String hashVotacio, String hashUsuari);

    void actualitzaUsrAlias(String hashVotacio, String hashUsuari, String alias);

    void vota(String hashVotacio, String hashUsuari, int numOpcioVotada);

    public static class VotacioDto {

        public final Votacio v;
        public final Usr u;
        public final List<Opcio> os;
        public final List<Usr> usrs;

        public VotacioDto(Votacio v, Usr u, List<Opcio> os, List<Usr> usrs) {
            super();
            this.v = v;
            this.u = u;
            this.os = os;
            this.usrs = usrs;
        }

    }

}
