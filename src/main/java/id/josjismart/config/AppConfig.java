package id.josjismart.config;

import java.io.*;
import java.nio.file.*;
import java.util.Properties;

public final class AppConfig {
    private static final Properties P = new Properties();
    static {
        P.setProperty("db.host", "127.0.0.1");
        P.setProperty("db.port", "3306");
        P.setProperty("db.name", "josjismart_db");
        P.setProperty("db.user", "root");
        P.setProperty("db.password", "");
        Path config = Paths.get("config.properties");
        if (Files.isRegularFile(config)) {
            try (InputStream in = Files.newInputStream(config)) { P.load(in); }
            catch (IOException e) { throw new IllegalStateException("Gagal membaca config.properties", e); }
        }
    }
    private AppConfig() {}
    public static String get(String name) { return P.getProperty(name, "").trim(); }
    public static String databaseName() {
        String name = get("db.name");
        if (!name.matches("[a-zA-Z0-9_]+")) throw new IllegalArgumentException("Nama database hanya boleh huruf, angka, underscore.");
        return name;
    }
    public static String baseUrl() {
        String host = get("db.host");
        String port = get("db.port");
        if (!host.matches("[a-zA-Z0-9.:-]+") || !port.matches("\\d{1,5}"))
            throw new IllegalArgumentException("Host / port MySQL tidak valid dalam config.properties");
        return "jdbc:mariadb://" + host + ":" + port + "/";
    }
    public static String options() { return "?sslMode=disable&allowPublicKeyRetrieval=true&connectTimeout=6000"; }
}
