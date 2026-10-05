package api;

public interface DatabaseService {

    boolean initializeDatabase();

    boolean setDBConfig(String url, String username, String password);

    boolean testDBConnection(String url, String username, String password);

    String getDBUrl();

    String getDBUsername();

    String getDBPassword();
}
