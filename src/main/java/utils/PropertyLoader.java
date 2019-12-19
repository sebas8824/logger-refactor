package utils;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class PropertyLoader {
    public static Properties mapPropertiesFromFile(String propertiesFileName) throws IOException {
        Properties properties = new Properties();
        InputStream logFile = new FileInputStream(propertiesFileName);
        properties.load(logFile);
        return properties;
    }
}
