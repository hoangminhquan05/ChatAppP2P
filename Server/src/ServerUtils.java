import java.io.*;
import java.util.Properties;

public class ServerUtils {
    private static final String CONFIG_FILE = "resources/server.properties";
    private static Properties properties;

    static {
        properties = new Properties();
        try {
            File configFile = new File(CONFIG_FILE);
            if (configFile.exists()) {
                properties.load(new FileInputStream(configFile));
            } else {
                // Default values
                setDefaultProperties();
            }
        } catch (IOException e) {
            System.err.println("Không thể load cấu hình: " + e.getMessage());
            setDefaultProperties();
        }
    }

    private static void setDefaultProperties() {
        properties.setProperty("server.port", "8888");
        properties.setProperty("max.connections", "100");
        properties.setProperty("heartbeat.timeout", "30000");
        properties.setProperty("log.level", "INFO");
    }

    public static int getServerPort() {
        return Integer.parseInt(properties.getProperty("server.port", "8888"));
    }

    public static int getMaxConnections() {
        return Integer.parseInt(properties.getProperty("max.connections", "100"));
    }

    public static long getHeartbeatTimeout() {
        return Long.parseLong(properties.getProperty("heartbeat.timeout", "30000"));
    }

    public static String getLogLevel() {
        return properties.getProperty("log.level", "INFO");
    }

    public static void log(String level, String message) {
        String timestamp = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                .format(new java.util.Date());
        System.out.println("[" + timestamp + "] [" + level + "] " + message);
    }

    public static void info(String message) {
        log("INFO", message);
    }

    public static void error(String message) {
        log("ERROR", message);
    }

    public static void warn(String message) {
        log("WARN", message);
    }
}