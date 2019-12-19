package utils;

import constant.Constants;

import java.io.FileInputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.Properties;

public class SetUpDBConnection {
    public static Connection setUpConnection() throws Exception {

        Properties prop = PropertyLoader.mapPropertiesFromFile(Constants.DB_PARAMS_PROPS);
        Properties dbUserProps = new Properties();

        dbUserProps.put("user", prop.getProperty(Constants.USERNAME_PROP));
        dbUserProps.put("password", prop.getProperty(Constants.PASSWORD_PROP));

        return DriverManager.getConnection(
                "jdbc:" + prop.getProperty(Constants.DBMS) +
                        "://" + prop.getProperty(Constants.SERVER_NAME) +
                        ":" + prop.getProperty(Constants.PORT_NUMBER) +
                        "/", dbUserProps
        );
    }
}
