package examples.ex5.wot;

public class WotTest {

    public static class Ticket {
        Long idTicket;
        String title;
        String creatDate;
    }

    public static class TicketUsrId {
        Long idTicket;
        Integer idUsr;
    }

    public static class TicketUsr {
        TicketUsrId id;
        ETicketUserRole role;
    }

    public static enum ETicketUserRole {
        ADMIN, OWNER, PARTICIPANT;
    }

    public static class Usr {
        Integer idUsr;
        String name;
        String email;
    }

    public static class TicketUsrAction {

        Long idTicketUsrAction;

        String date;
        String title;
        String message;
        ETicketStatus newStatus;

        Long idTicket;
        Integer idUsr;

    }

    public static enum ETicketStatus {
        INIT, CLOSE;
    }
}
