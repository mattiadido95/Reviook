package it.unipi.dii.reviook_app;

import it.unipi.dii.reviook_app.Data.Author;
import it.unipi.dii.reviook_app.Data.Users;

public class Session {
    private static Session session = null;
    private Boolean type;
    private Users loggedUser = null;
    private Author loggedAuthor = null;

    public static Session getSession() {
        return session;
    }



    public static void setSession(Session session) {
        Session.session = session;
    }

    public void setLoggedUser(Users logged) {
        this.loggedUser = logged;
    }
    public void setLoggedAuthor(Author logged) {
        this.loggedAuthor = logged;
    }

    private Session() {}

    public Boolean getType() {
        return type;
    }

    public void setType(Boolean type) {
        if(session == null)
        {
            new RuntimeException("Session is not active.");
        } else
            this.type = type;
    }

    public static Session getInstance() {
        if(session == null) {
            session = new Session();
        }
        return session;
    }

    public void setLoggedUser(String name, String surname, String nickname,String email, String password) {
        if(session == null) {
            new RuntimeException("Session is not active.");
        } else {
            session.loggedUser = new Users( name,  surname,  nickname, email,  password);
        }
    }

    public void setLoggedAuthor(String name, String surname, String nickname,String email, String password) {
        if(session == null) {
            new RuntimeException("Session is not active.");
        } else {
            session.loggedAuthor = new Author( name,  surname,  nickname, email,  password);
        }
    }

    public Users getLoggedUser() {
        if(session == null) {
            throw new RuntimeException("Session is not active.");
        } else {
            return session.loggedUser;
        }
    }
    public Author getLoggedAuthor() {
        if(session == null) {
            throw new RuntimeException("Session is not active.");
        } else {
            return session.loggedAuthor;
        }
    }
}