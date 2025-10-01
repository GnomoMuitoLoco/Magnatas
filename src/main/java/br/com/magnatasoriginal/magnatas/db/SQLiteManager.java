package br.com.magnatasoriginal.magnatas.db;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class SQLiteManager {
    private final JavaPlugin plugin;

    public SQLiteManager(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Inicializa o banco de dados e cria as tabelas necessárias.
     */
    public void initializeDatabase() {
        try (Connection conn = openConnection()) {
            Statement stmt = conn.createStatement();

            // Tabela de homes
            stmt.execute("CREATE TABLE IF NOT EXISTS homes (" +
                    "uuid TEXT, " +
                    "name TEXT, " +
                    "x REAL, " +
                    "y REAL, " +
                    "z REAL, " +
                    "world TEXT)");

            // Tabela de lojas (agora com displayName)
            stmt.execute("CREATE TABLE IF NOT EXISTS lojas (" +
                    "playerName TEXT PRIMARY KEY, " +
                    "displayName TEXT, " +
                    "world TEXT, " +
                    "x REAL, " +
                    "y REAL, " +
                    "z REAL, " +
                    "pitch REAL, " +
                    "yaw REAL, " +
                    "visitCount INTEGER DEFAULT 0)");

            // Tabela de logs
            stmt.execute("CREATE TABLE IF NOT EXISTS logs (" +
                    "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                    "playerName TEXT, " +
                    "lojaOwner TEXT, " +
                    "timestamp TEXT)");

            // Tabela de Tokens
            stmt.execute("CREATE TABLE IF NOT EXISTS tokens (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "tokenCount INTEGER DEFAULT 0, " +
                    "lastClaimed TEXT, " +
                    "streak INTEGER DEFAULT 0)");

            stmt.close();
        } catch (SQLException e) {
            plugin.getLogger().warning("Erro ao inicializar o banco de dados SQLite:");
            e.printStackTrace();
        }
    }

    /**
     * Abre uma nova conexão com o banco de dados SQLite.
     */
    public Connection openConnection() throws SQLException {
        File dbFile = new File(plugin.getDataFolder(), "sqlite.db");
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }
        return DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
    }
}