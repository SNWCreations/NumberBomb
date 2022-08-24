package snw.numberbomb;

import snw.jkook.entity.User;
import snw.jkook.util.Validate;

import java.util.HashSet;
import java.util.Set;

public class SessionStorage {
    private final Set<Session> sessions = new HashSet<>();

    public Session createSession(User user) {
        Validate.isFalse(hasSession(user), "This user has already created a session!");
        Session result = new Session(user, this);
        sessions.add(result);
        return result;
    }

    public void addSession(Session session) {
        Validate.isFalse(hasSession(session.getUser()), "The user that related to the requested session has already bound to another session.");
        sessions.add(session);
    }

    public boolean hasSession(User user) {
        return getSession(user) != null;
    }

    public Session getSession(User user) {
        return sessions.stream().filter(IT -> IT.getUser() == user).findFirst().orElse(null);
    }

    public boolean destorySession(User user) {
        return sessions.removeIf(IT -> IT.getUser() == user);
    }
}
