package br.com.magnatasoriginal.magnatas;

import br.com.magnatasoriginal.magnatas.listeners.TituloGUIListener;
import br.com.magnatasoriginal.magnatas.sistemas.antilag.limites.*;
import br.com.magnatasoriginal.magnatas.sistemas.homes.*;
import br.com.magnatasoriginal.magnatas.sistemas.lojas.*;
import br.com.magnatasoriginal.magnatas.sistemas.economia.*;
import br.com.magnatasoriginal.magnatas.db.SQLiteManager;
import br.com.magnatasoriginal.magnatas.listeners.PlayerJoinListener;

import br.com.magnatasoriginal.magnatas.sistemas.mensagens.AjudaAnuncioTask;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.MagnatasCommandDispatcher;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.MensagemChaves;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.MensagemProvider;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.*;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.lojadetitulos.LojadeTitulos;
import br.com.magnatasoriginal.magnatas.sistemas.warps.*;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.logging.Level;

public final class Magnatas extends JavaPlugin {
    private SQLiteManager sqliteManager;
    private FileConfiguration config;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private MensagemProvider mensagens;
    private LimitesManager limitesManager;
    private TituloManager tituloManager;
    private Tokens tokens;
    private File titulosAtivosFile;
    private FileConfiguration titulosAtivosConfig;
    private LojadeTitulos lojadeTitulos;
    private TokenLojaGUI tokenLojaGUI;
    private TituloMenu tituloMenu = new TituloMenu();


    @Override
    public void onEnable() {
        // Inicializa o gerenciador de banco de dados
        sqliteManager = new SQLiteManager(this);
        sqliteManager.initializeDatabase();

        // Inicializa os gerenciadores de sistemas Homes e Warps
        try {
            homeManager = new HomeManager(sqliteManager.openConnection());
            warpManager = new WarpManager(sqliteManager);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

        // 🔧 Carrega config principal
        saveDefaultConfig();
        config = getConfig(); // config.yml em /resources/

        // 📁 Garante pastas e arquivos necessários

        new File(getDataFolder(), "Sistemas/Titulos").mkdirs();
        saveResource("Sistemas/Titulos/titulos.yml", false);
        File configFile = new File(getDataFolder(), "Sistemas/Titulos/titulos.yml");

        File titulosAtivosFile = new File(getDataFolder(), "Sistemas/Titulos/titulos_ativos.yml");
        if (!titulosAtivosFile.exists()) {
            try {
                titulosAtivosFile.getParentFile().mkdirs();
                titulosAtivosFile.createNewFile();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Erro ao criar titulos_ativos.yml", e);
            }
        }
        titulosAtivosConfig = YamlConfiguration.loadConfiguration(titulosAtivosFile);

        // 💬 Carrega mensagens localizadas
        String caminhoMensagens = "Sistemas/Mensagens/pt_br.yml";
        saveResource(caminhoMensagens, false);
        File arquivoMensagens = new File(getDataFolder(), caminhoMensagens);
        YamlConfiguration mensagensConfig = YamlConfiguration.loadConfiguration(arquivoMensagens);
        mensagens = new MensagemProvider(mensagensConfig);
        verificarMensagens(mensagens);

        // 📢 Tarefa de ajuda automática
        int intervaloAjuda = config.getInt("ajuda_convite_intervalo", 300);
        BukkitTask tarefaAjuda = new AjudaAnuncioTask(this, mensagens)
                .runTaskTimer(this, 20L * intervaloAjuda, 20L * intervaloAjuda);

        // 🧱 Inicializa o sistema de limites
        LimitesStorage limitesStorage = new LimitesStorage(sqliteManager);
        limitesManager = new LimitesManager(limitesStorage);
        new LimitesBlockListener(limitesManager, this);

        // 💰 Inicializa o sistema de Tokens
        this.tokens = new Tokens(this);
        this.lojadeTitulos = new LojadeTitulos(this); // cria e registra só uma vez
        this.tokenLojaGUI = new TokenLojaGUI(this, lojadeTitulos);

    // 🏷️ Inicializa o sistema de Títulos
        tituloManager = new TituloManager(this, sqliteManager);
        new MagnatasTitulosExpansion(this, tituloManager, tokens).register();
        new TituloGUI(this); // Listener da interface de títulos (registra cliques)


        // 📜 Comandos de limites
        getCommand("limite").setExecutor(new LimiteCommand(limitesManager, this));
        getCommand("limites").setExecutor(new LimitesCommand(limitesManager, this));

        // 📜 Comando de mensagens
        getCommand("magnatas").setExecutor(new MagnatasCommandDispatcher(this, mensagens, tarefaAjuda, tituloManager));


        // 📜 Comandos de lojas, homes e warps
        Objects.requireNonNull(getCommand("setwarp")).setExecutor(new SetWarpCommand(this));
        Objects.requireNonNull(getCommand("delwarp")).setExecutor(new DelWarpCommand(this));
        Objects.requireNonNull(getCommand("warp")).setExecutor(new WarpCommand(this));
        Objects.requireNonNull(getCommand("warps")).setExecutor(new WarpsCommand(this));
        Objects.requireNonNull(getCommand("lojas")).setExecutor(new LojaCommand(this));
        Objects.requireNonNull(getCommand("setloja")).setExecutor(new SetLojaCommand(this));
        Objects.requireNonNull(getCommand("delloja")).setExecutor(new DelLojaCommand(this));
        Objects.requireNonNull(getCommand("tploja")).setExecutor(new TeleportLojaCommand(this));
        Objects.requireNonNull(getCommand("sethome")).setExecutor(new SetHomeCommand(this));
        Objects.requireNonNull(getCommand("home")).setExecutor(new HomeCommand(this));
        Objects.requireNonNull(getCommand("delhome")).setExecutor(new DelHomeCommand(this));
        Objects.requireNonNull(getCommand("homes")).setExecutor(new HomesCommand(this));

        // 📜 Comandos de Tokens
        Objects.requireNonNull(getCommand("token")).setExecutor(new TokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("addtoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("removetoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("settoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("vertoken")).setExecutor(new AdminTokenCommand(this, tokens));



        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new TituloGUIListener(this, tituloMenu), this);
        getServer().getPluginManager().registerEvents(new TituloGUI(this), this);


        // ✅ Log de inicialização
        getLogger().info("Sistema de Lojas Ativado!");
        getLogger().info("Sistema de Homes Ativado!");
        getLogger().info("Sistema de Warps - Ativado");
        getLogger().info("Sistema de Tokens - Ativado!");
        getLogger().info("Sistema de Mensagens - Em Desenvolvimento!");
        getLogger().info("Sistema de Limites - Em Desenvolvimento!");
        getLogger().info("Outros sistemas sendo desenvolvidos...");

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
    //Getters para os gerenciadores
    public TokenLojaGUI getTokenLojaGUI() {
        return tokenLojaGUI;
    }

    public LojadeTitulos getLojaDeTitulos() {
        return lojadeTitulos;
    }

    public FileConfiguration getTitulosAtivosConfig() {
        return titulosAtivosConfig;
    }

    public void salvarTitulosAtivos() {
        try {
            titulosAtivosConfig.save(titulosAtivosFile);
        } catch (Exception e) {
            getLogger().log(Level.WARNING, "Erro ao salvar titulos_ativos.yml", e);
        }
    }

    public Tokens getTokens() {
        return tokens;
    }

    public TituloManager getTituloManager() {
        return tituloManager;
    }

    public LimitesManager getLimitesManager() {
        return limitesManager;
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

    public WarpManager getWarpManager() {
        return warpManager;
    }

    public MensagemProvider getMensagens() {
        return mensagens;
    }

    public void verificarMensagens(MensagemProvider mensagens) {
        for (String chave : MensagemChaves.AJUDA) {
            if (mensagens.getConfig().getString(chave) == null) {
                getLogger().warning("⚠ Mensagem ausente no pt_br.yml: " + chave);
            }
        }
    }

    public static String formatarDuracao(long millis) {
        long totalSeconds = millis / 1000;
        long dias = totalSeconds / 86400;
        long horas = (totalSeconds % 86400) / 3600;
        long minutos = (totalSeconds % 3600) / 60;

        if (dias > 0) return dias + " dia" + (dias > 1 ? "s" : "");
        if (horas > 0) return horas + " hora" + (horas > 1 ? "s" : "");
        return minutos + " minuto" + (minutos > 1 ? "s" : "");
    }

    public String colorir(String texto) {
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', texto);
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