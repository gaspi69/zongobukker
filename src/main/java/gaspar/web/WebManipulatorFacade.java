package gaspar.web;

public interface WebManipulatorFacade<Session> {

    void login(final Session session);

    void search(final Session session);

    void calculate(final Session session);

    void modify(final Session session);

    void logout(final Session session);

}
