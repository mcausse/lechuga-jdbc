package linenetcom.ent;

import org.lechuga.annotated.anno.Generated;
import org.lechuga.annotated.anno.Id;
import org.lechuga.annotated.anno.Table;
import org.lechuga.mapper.autogen.HsqldbIdentity;

@Table("users")
public class User {

    @Id
    @Generated(value = HsqldbIdentity.class)
    Integer idUser;

    String email;

    public User() {
        super();
    }

    public User(Integer idUser, String email) {
        super();
        this.idUser = idUser;
        this.email = email;
    }

    public Integer getIdUser() {
        return idUser;
    }

    public void setIdUser(Integer idUser) {
        this.idUser = idUser;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Override
    public String toString() {
        return "User [idUser=" + idUser + ", email=" + email + "]";
    }

}
