package utils;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SetUpDBConnection {

    public static Connection setUpConnection() throws Exception {
        Properties prop = new Properties();
        Properties dbUserProps = new Properties();
        InputStream input = new FileInputStream("dbparams.properties");
        prop.load(input);

        dbUserProps.put("user", prop.getProperty("userName"));
        dbUserProps.put("password", prop.getProperty("password"));
        Connection connection = DriverManager.getConnection(
                "jdbc:" + prop.getProperty("dbms") +
                        "://" + prop.getProperty("serverName") +
                        ":" + prop.getProperty("portNumber") +
                        "/", dbUserProps
        );
        return connection;
    }

    private static void loadDBPropertiesFile() {

    }
}
