package de.wolfgangkronberg.kspc.library;

import de.wolfgangkronberg.kspc.GlobalElements;

import java.sql.*;

public class LibraryDB {

    private static final int CURRENT_DB_VERSION = 1;

    private final GlobalElements ge;
    private final String url;
    private final String user = "sa";
    private final String password = "";

    public LibraryDB(GlobalElements ge) {
        this.ge = ge;
        url = "jdbc:h2:file:" + ge.getProps().getHome().getAbsolutePath() + "/library";
    }

    public void init() {
        try (Connection conn = createConnection()) {
            int version = getDBVersion(conn);
            switch (version) {
                case 0:
                    createNewDB(conn);
                    break;
                case CURRENT_DB_VERSION:
                    // nothing to do
                    break;
                default:
                    if (version > CURRENT_DB_VERSION) {
                        System.out.println("Sorted library has a newer version than this software. Consider upgrading.");
                    } else {
                        System.err.println("Library DB seems to be broken: version = " + version + ". Exiting.");
                        System.exit(-1);
                    }
                    break;
            }
            System.out.println("Using existing database with DB version " + version);
            scanDB(conn);
        } catch (SQLException e) {
            System.err.println("Could not access library database: " + e.toString() + ". Exiting.");
            System.exit(-1);
        }
    }

    private void scanDB(Connection conn) {
        // !kgb
    }

    private void createNewDB(Connection conn) throws SQLException {
        try (Statement statement = conn.createStatement()) {
            statement.execute("CREATE TABLE CONFIG ( KEY VARCHAR(255) NOT NULL, VALUE VARCHAR(255) NOT NULL, PRIMARY KEY (KEY) )");
            statement.execute("INSERT INTO CONFIG VALUES ('version', '1')");
        }
    }

    // return 0 if library absent
    private int getDBVersion(Connection conn) throws SQLException {
        try (ResultSet rs = conn.getMetaData().getTables(
                null, null, "CONFIG", null)) {
            if (!rs.first()) {
                return 0;
            }
        }
        try (Statement statement = conn.createStatement()) {
            try (ResultSet rs = statement.executeQuery("SELECT VALUE FROM CONFIG WHERE KEY='version'")) {
                if (!rs.first()) {
                    throw new SQLException("Version key is missing from config table");
                }
                return rs.getInt(1);
            }
        }
    }

    private Connection createConnection() throws SQLException {
        try {
            Class.forName ("org.h2.Driver");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            System.exit(-1);
        }
        return DriverManager.getConnection(url, user, password);
    }

    public boolean isEmpty() {
        return true; // !kgb
    }
}
