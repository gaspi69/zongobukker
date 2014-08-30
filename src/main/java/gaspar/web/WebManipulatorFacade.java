package gaspar.web;

public interface WebManipulatorFacade<Configuration> {

    void login(final Configuration configuration);

    void search(final Configuration configuration);

    void calculate(final Configuration configuration);

    void modify(final Configuration configuration);

    void logout(final Configuration configuration);

}
