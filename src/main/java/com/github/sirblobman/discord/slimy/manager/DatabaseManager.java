package com.github.sirblobman.discord.slimy.manager;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Locale;
import java.util.Set;

import com.github.sirblobman.discord.slimy.DiscordBot;

import org.apache.logging.log4j.Logger;
import org.sqlite.JDBC;
import org.sqlite.SQLiteDataSource;

public final class DatabaseManager extends Manager {
    private final SQLiteDataSource dataSource;
    
    public DatabaseManager(DiscordBot discordBot) {
        super(discordBot);
        this.dataSource = new SQLiteDataSource();
        
        Path path = Paths.get("database.sqlite");
        String filePath = path.toAbsolutePath().toString();
        
        this.dataSource.setDatabaseName("Slimy Bot Database");
        this.dataSource.setUrl(JDBC.PREFIX + filePath);
    }
    
    public synchronized boolean connectToDatabase() {
        Logger logger = getLogger();
        try (Connection connection = getConnection()) {
            DatabaseMetaData connectionMeta = connection.getMetaData();
            String driverName = connectionMeta.getDriverName();
            String driverVersion = connectionMeta.getDriverVersion();
            String driverFullName = String.format(Locale.US, "%s v%s", driverName, driverVersion);
            logger.info("Successfully connected to SQLite database with driver " + driverFullName + ".");
    
            String url = this.dataSource.getUrl();
            logger.info("Database URL: " + url);
    
            logger.info("Creating any non-existing database tables...");
            checkTables(connection);
            
            logger.info("Database table creation completed.");
            return true;
        } catch(SQLException ex) {
            logger.error("An error occurred while connecting to the SQLite database:", ex);
            return false;
        }
    }
    
    synchronized Connection getConnection() throws SQLException {
        return this.dataSource.getConnection();
    }
    
    synchronized String getCommandFromSQL(String commandName, Object... replacements) {
        try {
            Class<?> currentClass = getClass();
            String fileName = ("/sql/" + commandName + ".sql");
            
            InputStream jarFile = currentClass.getResourceAsStream(fileName);
            if(jarFile == null) {
                throw new IOException("'" + fileName + "' does not exist in the jar file.");
            }
            
            InputStreamReader jarFileReader = new InputStreamReader(jarFile);
            BufferedReader bufferedReader = new BufferedReader(jarFileReader);
            StringBuilder finalCommand = new StringBuilder();
            
            String currentLine;
            while((currentLine = bufferedReader.readLine()) != null) {
                finalCommand.append(currentLine);
                finalCommand.append("\n");
            }
            
            String sqlCode = finalCommand.toString().trim();
            return String.format(Locale.US, sqlCode, replacements);
        } catch(IOException ex) {
            Logger logger = getLogger();
            logger.error("An error occurred while getting an SQL command:", ex);
            return "";
        }
    }
    
    private synchronized void checkTables(Connection connection) throws SQLException {
        Set<String> commands = Set.of("create_known_guilds", "create_known_members", "create_known_channels",
                "create_message_history");
        
        for(String command : commands) {
            String sqlCommand = getCommandFromSQL(command);
            PreparedStatement statement = connection.prepareStatement(sqlCommand);
            statement.execute();
            statement.close();
        }
    }
}
