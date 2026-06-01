package com.parking.config;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLClassLoader;
import java.sql.Connection;
import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.DriverPropertyInfo;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;
import java.util.logging.Logger;

public final class Database {
    private static final String URL = "jdbc:sqlite:parking_management.db";
    private static boolean driverLoaded;
    private static URLClassLoader sqliteClassLoader;

    private Database() {
    }

    public static Connection getConnection() throws SQLException {
        loadSqliteDriver();
        Connection connection = DriverManager.getConnection(URL);
        try (Statement statement = connection.createStatement()) {
            statement.execute("PRAGMA foreign_keys = ON");
        }
        return connection;
    }

    private static void loadSqliteDriver() throws SQLException {
        if (driverLoaded) {
            return;
        }
        try {
            Class.forName("org.sqlite.JDBC");
            driverLoaded = true;
        } catch (ClassNotFoundException exception) {
            loadDriverFromLibFolder(exception);
        }
    }

    private static void loadDriverFromLibFolder(ClassNotFoundException originalException) throws SQLException {
        File jarFile = findDriverJar();
        if (!jarFile.exists()) {
            throw new SQLException("SQLite JDBC driver not found. Keep sqlite-jdbc.jar inside the lib folder.",
                    originalException);
        }
        try {
            sqliteClassLoader = new URLClassLoader(new URL[] { jarFile.toURI().toURL() },
                    Database.class.getClassLoader());
            Driver driver = (Driver) Class.forName("org.sqlite.JDBC", true, sqliteClassLoader)
                    .getDeclaredConstructor()
                    .newInstance();
            DriverManager.registerDriver(new DriverShim(driver));
            driverLoaded = true;
        } catch (ReflectiveOperationException | java.net.MalformedURLException exception) {
            throw new SQLException("SQLite JDBC driver exists but could not be loaded from lib/sqlite-jdbc.jar.",
                    exception);
        }
    }

    private static File findDriverJar() {
        File jarFromWorkingDirectory = findDriverJarFrom(new File(System.getProperty("user.dir")));
        if (jarFromWorkingDirectory.exists()) {
            return jarFromWorkingDirectory;
        }
        try {
            File classLocation = new File(Database.class.getProtectionDomain().getCodeSource().getLocation().toURI());
            File jarFromClassLocation = findDriverJarFrom(classLocation);
            if (jarFromClassLocation.exists()) {
                return jarFromClassLocation;
            }
        } catch (URISyntaxException ignored) {
            // Fall back to the project-root relative location below.
        }
        return new File("lib", "sqlite-jdbc.jar");
    }

    private static File findDriverJarFrom(File start) {
        File current = start.isFile() ? start.getParentFile() : start;
        while (current != null) {
            File candidate = new File(new File(current, "lib"), "sqlite-jdbc.jar");
            if (candidate.exists()) {
                return candidate;
            }
            current = current.getParentFile();
        }
        return new File("lib", "sqlite-jdbc.jar");
    }

    private static class DriverShim implements Driver {
        private final Driver driver;

        DriverShim(Driver driver) {
            this.driver = driver;
        }

        @Override
        public Connection connect(String url, Properties info) throws SQLException {
            return driver.connect(url, info);
        }

        @Override
        public boolean acceptsURL(String url) throws SQLException {
            return driver.acceptsURL(url);
        }

        @Override
        public DriverPropertyInfo[] getPropertyInfo(String url, Properties info) throws SQLException {
            return driver.getPropertyInfo(url, info);
        }

        @Override
        public int getMajorVersion() {
            return driver.getMajorVersion();
        }

        @Override
        public int getMinorVersion() {
            return driver.getMinorVersion();
        }

        @Override
        public boolean jdbcCompliant() {
            return driver.jdbcCompliant();
        }

        @Override
        public Logger getParentLogger() {
            return Logger.getGlobal();
        }
    }
}
