package br.com.magnatasoriginal.magnatas.db;

import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;

public class SQLiteManager {
    private final JavaPlugin plugin;
    private final File dbFile;
    private final String jdbcUrl;

    public SQLiteManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "sqlite.db");
        this.jdbcUrl = "jdbc:sqlite:" + dbFile.getAbsolutePath();

        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Não foi possível criar a pasta de dados do plugin!");
        }
    }

    /**
     * Inicializa o banco de dados e cria as tabelas necessárias.
     */
    public void initializeDatabase() {
        try (Connection conn = openConnection();
             Statement stmt = conn.createStatement()) {

            // Tabela de títulos disponíveis no servidor
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS titulos (
                    nome TEXT PRIMARY KEY,
                    descricao TEXT,
                    expira_em INTEGER
                )
            """);

            // Títulos adquiridos por jogador (com data de aquisição)
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jogador_titulos (
                    uuid TEXT,
                    titulo_nome TEXT,
                    adquirido_em INTEGER,
                    PRIMARY KEY(uuid, titulo_nome),
                    FOREIGN KEY (titulo_nome) REFERENCES titulos(nome)
                )
            """);

            // Título atualmente equipado por jogador
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS jogador_titulo_equipado (
                    uuid TEXT PRIMARY KEY,
                    titulo_nome TEXT,
                    FOREIGN KEY (titulo_nome) REFERENCES titulos(nome)
                )
            """);

            // Tabela de limites
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS limites_blocos (
                    bloco_id TEXT PRIMARY KEY,
                    quantidade INTEGER
                )
            """);

            // Tabela de warps públicas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS warps (
                    name TEXT PRIMARY KEY,
                    x REAL,
                    y REAL,
                    z REAL,
                    world TEXT
                )
            """);

            // Tabela de homes
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS homes (
                    uuid TEXT,
                    name TEXT,
                    x REAL,
                    y REAL,
                    z REAL,
                    world TEXT
                )
            """);

            // Tabela de lojas
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS lojas (
                    playerName TEXT PRIMARY KEY,
                    displayName TEXT,
                    world TEXT,
                    x REAL,
                    y REAL,
                    z REAL,
                    pitch REAL,
                    yaw REAL,
                    visitCount INTEGER DEFAULT 0
                )
            """);

            // Tabela de logs
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS logs (
                    id INTEGER PRIMARY KEY AUTOINCREMENT,
                    playerName TEXT,
                    lojaOwner TEXT,
                    timestamp TEXT
                )
            """);

            // Tabela de Tokens
            stmt.execute("""
                CREATE TABLE IF NOT EXISTS tokens (
                    uuid TEXT PRIMARY KEY,
                    tokenCount INTEGER DEFAULT 0,
                    lastClaimed TEXT,
                    streak INTEGER DEFAULT 0
                )
            """);

        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao inicializar o banco de dados SQLite", e);
        }
    }

    /**
     * Abre uma nova conexão com o banco de dados SQLite.
     * Cada chamada retorna uma conexão independente (sem conexões persistentes).
     */
    public Connection openConnection() throws SQLException {
        return DriverManager.getConnection(jdbcUrl);
    }
}