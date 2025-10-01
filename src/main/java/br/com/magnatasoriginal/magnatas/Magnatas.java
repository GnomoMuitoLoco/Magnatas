package br.com.magnatasoriginal.magnatas;

import br.com.magnatasoriginal.magnatas.sistemas.homes.*;
import br.com.magnatasoriginal.magnatas.sistemas.lojas.*;
import br.com.magnatasoriginal.magnatas.sistemas.economia.*;
import br.com.magnatasoriginal.magnatas.db.SQLiteManager;
import br.com.magnatasoriginal.magnatas.listeners.PlayerJoinListener;

import br.com.magnatasoriginal.magnatas.sistemas.mensagens.AjudaAnuncioTask;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.MagnatasCommandDispatcher;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.logging.Level;

public final class Magnatas extends JavaPlugin {
    private SQLiteManager sqliteManager;
    private FileConfiguration config;
    private HomeManager homeManager;

    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = getConfig();

        int intervalo = getConfig().getInt("mensagens.ajuda_convite.intervalo", 300);
        BukkitTask tarefaAjuda = new AjudaAnuncioTask(this, config)
                .runTaskTimer(this, 20L * intervalo, 20L * intervalo);

        getLogger().info("Sistema de Lojas Ativado!");
        getLogger().info("Sistema de Homes Ativado!");
        getLogger().info("Sistema de Warps - Em Desenvolvimento!");
        getLogger().info("Sistema de Tokens - Ativado!");
        getLogger().info("Sistema de Mensagens - Em Desenvolvimento!");
        getLogger().info("Sistema de Limites - Em Desenvolvimento!");
        getLogger().info("Idealizando projeto...");

        sqliteManager = new SQLiteManager(this);
        sqliteManager.initializeDatabase();

        try {
            homeManager = new HomeManager(sqliteManager.openConnection());
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        Tokens tokens = new Tokens(this); // Instancia sistema de Tokens


        // Comandos de mensagens
        getCommand("magnatas").setExecutor(new MagnatasCommandDispatcher(this, tarefaAjuda));
        // Comandos de lojas e homes
        Objects.requireNonNull(getCommand("lojas")).setExecutor(new LojaCommand(this));
        Objects.requireNonNull(getCommand("setloja")).setExecutor(new SetLojaCommand(this));
        Objects.requireNonNull(getCommand("delloja")).setExecutor(new DelLojaCommand(this));
        Objects.requireNonNull(getCommand("tploja")).setExecutor(new TeleportLojaCommand(this));
        Objects.requireNonNull(getCommand("sethome")).setExecutor(new SetHomeCommand(this));
        Objects.requireNonNull(getCommand("home")).setExecutor(new HomeCommand(this));
        Objects.requireNonNull(getCommand("delhome")).setExecutor(new DelHomeCommand(this));
        Objects.requireNonNull(getCommand("homes")).setExecutor(new HomesCommand(this));

        // Comandos de Tokens
        Objects.requireNonNull(getCommand("token")).setExecutor(new TokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("addtoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("removetoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("settoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("vertoken")).setExecutor(new AdminTokenCommand(this, tokens));

        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);

        getLogger().info("    __  ___                        __            ");
        getLogger().info("   /  |/  /___ _____ _____  ____ _/ /_____ ______");
        getLogger().info("  / /|_/ / __ `/ __ `/ __ \\/ __ `/ __/ __ `/ ___/");
        getLogger().info(" / /  / / /_/ / /_/ / / / / /_/ / /_/ /_/ (__  ) ");
        getLogger().info("/_/  /_/\\__,_/\\__, /_/ /_/\\__,_/\\__/\\__,_/____/  ");
        getLogger().info("             /____/                              ");
        getLogger().info("=======================================");
        getLogger().info("=         MAGNATAS ORIGINAL           =");
        getLogger().info("=     Plugin ativado com sucesso!     =");
        getLogger().info("=             by Gnomo                =");
        getLogger().info("=======================================");
    }

    @Override
    public void onDisable() {
        getLogger().info("Plugin Magnatas Desativado!");
    }

    public String normalize(String name) {
        return name.toLowerCase();
    }

    public SQLiteManager getSQLiteManager() {
        return sqliteManager;
    }

    public HomeManager getHomeManager() {
        return homeManager;
    }


    public String getMessage(String key, String... args) {
        String message = config.getString("messages." + key, "Mensagem não encontrada: " + key);
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("%" + (i + 1), args[i]);
        }
        return message;
    }

    // ================================
    // Métodos de banco de dados - Loja
    // ================================

    public void saveLoja(String playerName, Location loc, Runnable onSuccess) {
        String normalizedName = normalize(playerName);
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = sqliteManager.openConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT OR REPLACE INTO lojas (playerName, displayName, world, x, y, z, pitch, yaw, visitCount) " +
                                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, 0)")) {
                    stmt.setString(1, normalizedName);
                    stmt.setString(2, playerName);
                    stmt.setString(3, Objects.requireNonNull(loc.getWorld()).getName());
                    stmt.setDouble(4, loc.getX());
                    stmt.setDouble(5, loc.getY());
                    stmt.setDouble(6, loc.getZ());
                    stmt.setFloat(7, loc.getPitch());
                    stmt.setFloat(8, loc.getYaw());
                    stmt.executeUpdate();

                    new BukkitRunnable() {
                        @Override
                        public void run() {
                            onSuccess.run();
                        }
                    }.runTask(Magnatas.this);

                } catch (SQLException e) {
                    getLogger().log(Level.WARNING, "Erro ao salvar loja de " + playerName, e);
                }
            }
        }.runTaskAsynchronously(this);
    }

    public Map<String, Location> getAllLojas() {
        Map<String, Location> lojas = new HashMap<>();
        try (Connection conn = sqliteManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT displayName, world, x, y, z, pitch, yaw FROM lojas")) {
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    World world = getServer().getWorld(rs.getString("world"));
                    if (world == null) continue;
                    String displayName = rs.getString("displayName");
                    Location loc = new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                    lojas.put(displayName, loc);
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING,"Erro ao buscar todas as lojas", e);
        }
        return lojas;
    }

    public Location getLojaLocation(String playerName) {
        String normalizedName = normalize(playerName);
        try (Connection conn = sqliteManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT world, x, y, z, pitch, yaw FROM lojas WHERE playerName = ?")) {
            stmt.setString(1, normalizedName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    World world = getServer().getWorld(rs.getString("world"));
                    if (world == null) return null;
                    return new Location(world, rs.getDouble("x"), rs.getDouble("y"), rs.getDouble("z"),
                            rs.getFloat("yaw"), rs.getFloat("pitch"));
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING,"Erro ao buscar loja de " + playerName, e);
        }
        return null;
    }

    public int getVisitCount(String playerName) {
        String normalizedName = normalize(playerName);
        try (Connection conn = sqliteManager.openConnection();
             PreparedStatement stmt = conn.prepareStatement(
                     "SELECT visitCount FROM lojas WHERE playerName = ?")) {
            stmt.setString(1, normalizedName);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt("visitCount");
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING,"Erro ao buscar visitas da loja de " + playerName, e);
        }
        return 0;
    }

    public void incrementVisitCount(String playerName) {
        String normalizedName = normalize(playerName);
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = sqliteManager.openConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "UPDATE lojas SET visitCount = visitCount + 1 WHERE playerName = ?")) {
                    stmt.setString(1, normalizedName);
                    stmt.executeUpdate();
                } catch (SQLException e) {
                    getLogger().log(Level.WARNING,"Erro ao incrementar visitas da loja de " + playerName, e);
                }
            }
        }.runTaskAsynchronously(this);
    }

    public void logVisit(String playerName, String lojaOwner) {
        String normalizedPlayer = normalize(playerName);
        String normalizedLoja = normalize(lojaOwner);
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = sqliteManager.openConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT INTO logs (playerName, lojaOwner, timestamp) VALUES (?, ?, ?)")) {
                    stmt.setString(1, normalizedPlayer);
                    stmt.setString(2, normalizedLoja);
                    stmt.setString(3, new java.sql.Timestamp(System.currentTimeMillis()).toString());
                    stmt.executeUpdate();

                    incrementVisitCount(normalizedLoja);
                } catch (SQLException e) {
                    getLogger().log(Level.WARNING,"Erro ao registrar visita de " + playerName + " à loja de " + lojaOwner, e);
                }
            }
        }.runTaskAsynchronously(this);
    }
}