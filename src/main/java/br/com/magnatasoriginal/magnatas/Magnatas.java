package br.com.magnatasoriginal.magnatas;

import br.com.magnatasoriginal.magnatas.db.SQLiteManager;
import br.com.magnatasoriginal.magnatas.listeners.PlayerJoinListener;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.cache.TituloCache;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.commands.TituloCommand;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.commands.TituloCommandAdmin;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloGUIListener;
import br.com.magnatasoriginal.magnatas.sistemas.antilag.limites.*;
import br.com.magnatasoriginal.magnatas.sistemas.economia.*;
import br.com.magnatasoriginal.magnatas.sistemas.homes.*;
import br.com.magnatasoriginal.magnatas.sistemas.lojas.*;
import br.com.magnatasoriginal.magnatas.sistemas.mensagens.*;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.*;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloGUIService;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.gui.TituloMenu;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.listener.TituloLojaListener;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.lojadetitulos.LojadeTitulos;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.tasks.TituloTaskScheduler;
import br.com.magnatasoriginal.magnatas.sistemas.warps.*;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;
import net.luckperms.api.LuckPerms;


import java.io.File;
import java.sql.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Magnatas extends JavaPlugin {

    private SQLiteManager sqliteManager;
    private FileConfiguration config;
    private HomeManager homeManager;
    private WarpManager warpManager;
    private MensagemProvider mensagens;
    private LimitesManager limitesManager;
    private TituloManager tituloManager;
    private TituloService tituloService;
    private Tokens tokens;
    private File titulosAtivosFile;
    private FileConfiguration titulosAtivosConfig;
    private LojadeTitulos lojadeTitulos;
    private TokenLojaGUI tokenLojaGUI;
    private TituloMenu tituloMenu;
    private TituloTaskScheduler scheduler;
    private BukkitTask tarefaAjuda;
    private TituloGUIService tituloGUIService;
    private LuckPerms luckPerms;


    private BukkitTask ajudaTask;

    @Override
    public void onEnable() {

        // 1) Banco de dados
        sqliteManager = new SQLiteManager(this);
        sqliteManager.initializeDatabase();

        // 2) Config principal
        saveDefaultConfig();
        config = getConfig();

        //Sistema de Ascii
        long start = System.nanoTime();

        // 3) Arquivos de títulos
        File titulosDir = new File(getDataFolder(), "Sistemas/Titulos");
        if (!titulosDir.exists() && !titulosDir.mkdirs()) {
            getLogger().warning("Não foi possível criar a pasta de títulos!");
        }
        saveResource("Sistemas/Titulos/titulos.yml", false);

        titulosAtivosFile = new File(titulosDir, "titulos_ativos.yml");
        if (!titulosAtivosFile.exists()) {
            try {
                titulosAtivosFile.createNewFile();
            } catch (Exception e) {
                getLogger().log(Level.WARNING, "Erro ao criar titulos_ativos.yml", e);
            }
        }
        titulosAtivosConfig = YamlConfiguration.loadConfiguration(titulosAtivosFile);

        // Obtém a instância do LuckPerms
        this.luckPerms = getServer().getServicesManager().load(LuckPerms.class);
        if (luckPerms == null) {
            getLogger().severe("LuckPerms não encontrado! O plugin não funcionará corretamente.");
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        // 4) Mensagens
        String caminhoMensagens = "Sistemas/Mensagens/pt_br.yml";
        saveResource(caminhoMensagens, false);
        File arquivoMensagens = new File(getDataFolder(), "Sistemas/Mensagens/pt_br.yml");
        mensagens = new MensagemProvider(arquivoMensagens, "Mensagem não encontrada");
        verificarMensagens(mensagens);

        // 5) Sistemas principais
        try {
            homeManager = new HomeManager(sqliteManager.openConnection());
            warpManager = new WarpManager(sqliteManager);
        } catch (SQLException e) {
            getLogger().log(Level.SEVERE, "Erro ao inicializar sistemas de Home/Warp", e);
            getServer().getPluginManager().disablePlugin(this);
            return;
        }

        limitesManager = new LimitesManager(new LimitesStorage(sqliteManager));
        new LimitesBlockListener(limitesManager, this);

        tokens = new Tokens(this);

        // --- TITULOS ---
        //Database
        tituloManager = new TituloManager(this, sqliteManager);
        //tituloManager.carregarTitulos(new File(getDataFolder(), "Sistemas/Titulos/titulos.yml"));
        TituloConfigLoader loader = new TituloConfigLoader(this);
        loader.getTitulos().forEach(tituloManager::registrarTitulo);
        //Cache
        TituloCache tituloCache = new TituloCache();
        tituloService = new TituloService(tituloManager, tituloCache);
        //Serviço de GUI
        tituloMenu = new TituloMenu(tituloService);
        this.tituloGUIService = new TituloGUIService(this, tituloService, tituloMenu);
        //Loja de titulos
        lojadeTitulos = new LojadeTitulos(this, tituloService, mensagens, tokens);
        //Loja de tokens
        tokenLojaGUI = new TokenLojaGUI(this, tituloGUIService);
        //Suporte a placeholder do Titulo e Token
        new MagnatasTitulosExpansion(this, tituloService).register();
        //Scheduler de Expiração de titulos
        TituloTaskScheduler scheduler = new TituloTaskScheduler(this, tituloService, tituloCache);
        scheduler.iniciarVerificacaoExpiracao(20L * 60L); // a cada 60 segundos
        //Listener (Interação na GUI da loja)
        // Registra o listener da loja
        getServer().getPluginManager().registerEvents(
                new TituloLojaListener(tituloService, tituloManager, tokens, luckPerms),
                this
        );


        // 6) Tarefa de ajuda automática
        int intervaloAjuda = config.getInt("ajuda_convite_intervalo", 300);
        ajudaTask = new AjudaAnuncioTask(this, mensagens)
                .runTaskTimer(this, 20L * intervaloAjuda, 20L * intervaloAjuda);

        // 8) Listeners
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(), this);
        getServer().getPluginManager().registerEvents(new TituloGUIListener(tituloGUIService), this);

        //Registrar comandos
        registrarComandos();

        // 9) Logs
        getLogger().info("=======================================");
        getLogger().info("=         MAGNATAS ORIGINAL           =");
        getLogger().info("=     Plugin ativado com sucesso!     =");
        getLogger().info("=             by Gnomo                =");
        getLogger().info("=======================================");

        logAsciiBanner(start);
    }

    @Override
    public void onDisable() {
        if (ajudaTask != null) {
            ajudaTask.cancel();
        if (scheduler != null) {
            scheduler.parar();
            }
        }
        getLogger().info("Plugin Magnatas Desativado!");
    }

    // ---------------------------
    // Registro de comandos
    // ---------------------------

    private void registrarComandos() {
        // Mensagens / Dispatcher
        Objects.requireNonNull(getCommand("magnatas"))
                .setExecutor(new MagnatasCommandDispatcher(this, mensagens, ajudaTask, tituloManager, tituloService));

        // Títulos
        Objects.requireNonNull(getCommand("titulos")).setExecutor(new TituloCommand(tituloService, tituloGUIService));
        Objects.requireNonNull(getCommand("titulosadmin")).setExecutor(new TituloCommandAdmin(tituloService));

        // Limites
        Objects.requireNonNull(getCommand("limite")).setExecutor(new LimiteCommand(limitesManager, this));
        Objects.requireNonNull(getCommand("limites")).setExecutor(new LimitesCommand(limitesManager, this));

        // Warps
        Objects.requireNonNull(getCommand("setwarp")).setExecutor(new SetWarpCommand(this));
        Objects.requireNonNull(getCommand("delwarp")).setExecutor(new DelWarpCommand(this));
        Objects.requireNonNull(getCommand("warp")).setExecutor(new WarpCommand(this));
        Objects.requireNonNull(getCommand("warps")).setExecutor(new WarpsCommand(this));

        // Lojas
        Objects.requireNonNull(getCommand("lojas")).setExecutor(new LojaCommand(this));
        Objects.requireNonNull(getCommand("setloja")).setExecutor(new SetLojaCommand(this));
        Objects.requireNonNull(getCommand("delloja")).setExecutor(new DelLojaCommand(this));
        Objects.requireNonNull(getCommand("tploja")).setExecutor(new TeleportLojaCommand(this));

        // Homes
        Objects.requireNonNull(getCommand("sethome")).setExecutor(new SetHomeCommand(this));
        Objects.requireNonNull(getCommand("home")).setExecutor(new HomeCommand(this));
        Objects.requireNonNull(getCommand("delhome")).setExecutor(new DelHomeCommand(this));
        Objects.requireNonNull(getCommand("homes")).setExecutor(new HomesCommand(this));

        // Tokens
        Objects.requireNonNull(getCommand("token")).setExecutor(new TokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("addtoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("removetoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("settoken")).setExecutor(new AdminTokenCommand(this, tokens));
        Objects.requireNonNull(getCommand("vertoken")).setExecutor(new AdminTokenCommand(this, tokens));
    }


    // ---------------------------
    // Getters utilitários
    // ---------------------------
    public TituloService getTituloService() { return tituloService; }
    public TokenLojaGUI getTokenLojaGUI() { return tokenLojaGUI; }
    public LojadeTitulos getLojaDeTitulos() { return lojadeTitulos; }
    public FileConfiguration getTitulosAtivosConfig() { return titulosAtivosConfig; }
    public void salvarTitulosAtivos() {
        try { titulosAtivosConfig.save(titulosAtivosFile); }
        catch (Exception e) { getLogger().log(Level.WARNING, "Erro ao salvar titulos_ativos.yml", e); }
    }
    public Tokens getTokens() { return tokens; }
    public TituloManager getTituloManager() { return tituloManager; }
    public LimitesManager getLimitesManager() { return limitesManager; }
    public SQLiteManager getSQLiteManager() { return sqliteManager; }
    public HomeManager getHomeManager() { return homeManager; }
    public WarpManager getWarpManager() { return warpManager; }
    public MensagemProvider getMensagens() { return mensagens; }

    // ---------------------------
    // Utilidades
    // ---------------------------
    public String normalize(String name) {
        return name == null ? "" : name.toLowerCase();
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

    // -------------------------------
    // Métodos de banco de dados - Loja
    // -------------------------------
    public void saveLoja(String playerName, Location loc, Runnable onSuccess) {
        String normalizedName = normalize(playerName);
        new BukkitRunnable() {
            @Override
            public void run() {
                try (Connection conn = sqliteManager.openConnection();
                     PreparedStatement stmt = conn.prepareStatement(
                             "INSERT OR REPLACE INTO lojas " +
                                     "(playerName, displayName, world, x, y, z, pitch, yaw, visitCount) " +
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

                    // callback no main thread
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
                     "SELECT displayName, world, x, y, z, pitch, yaw FROM lojas");
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                World world = getServer().getWorld(rs.getString("world"));
                if (world == null) continue;
                String displayName = rs.getString("displayName");
                Location loc = new Location(
                        world,
                        rs.getDouble("x"),
                        rs.getDouble("y"),
                        rs.getDouble("z"),
                        rs.getFloat("yaw"),
                        rs.getFloat("pitch")
                );
                lojas.put(displayName, loc);
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Erro ao buscar todas as lojas", e);
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
                    return new Location(
                            world,
                            rs.getDouble("x"),
                            rs.getDouble("y"),
                            rs.getDouble("z"),
                            rs.getFloat("yaw"),
                            rs.getFloat("pitch")
                    );
                }
            }
        } catch (SQLException e) {
            getLogger().log(Level.WARNING, "Erro ao buscar loja de " + playerName, e);
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
            getLogger().log(Level.WARNING, "Erro ao buscar visitas da loja de " + playerName, e);
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
                    getLogger().log(Level.WARNING, "Erro ao incrementar visitas da loja de " + playerName, e);
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
                    getLogger().log(Level.WARNING,
                            "Erro ao registrar visita de " + playerName + " à loja de " + lojaOwner, e);
                }
            }
        }.runTaskAsynchronously(this);
    }
    private void logAsciiBanner(long startNanos) {
        String version = getDescription().getVersion();
        String name = getDescription().getName();
        String authors = String.join(", ", getDescription().getAuthors());
        String serverVersion = getServer().getVersion();
        String bukkitVersion = getServer().getBukkitVersion();
        String apiVersion = getDescription().getAPIVersion();
        boolean debug = getConfig() != null && getConfig().getBoolean("debug", false);
        long elapsedMs = (System.nanoTime() - startNanos) / 1_000_000L;

        Logger log = getLogger();

        log.info(" ");
        log.info("███╗   ███╗ █████╗  ██████╗ ███╗   ██╗ █████╗ ████████╗ █████╗ ███████╗");
        log.info("████╗ ████║██╔══██╗██╔════╝ ████╗  ██║██╔══██╗╚══██╔══╝██╔══██╗██╔════╝");
        log.info("██╔████╔██║███████║██║  ███╗██╔██╗ ██║███████║   ██║   ███████║███████╗");
        log.info("██║╚██╔╝██║██╔══██║██║   ██║██║╚██╗██║██╔══██║   ██║   ██╔══██║╚════██║");
        log.info("██║ ╚═╝ ██║██║  ██║╚██████╔╝██║ ╚████║██║  ██║   ██║   ██║  ██║███████║");
        log.info("╚═╝     ╚═╝╚═╝  ╚═╝ ╚═════╝ ╚═╝  ╚═══╝╚═╝  ╚═╝   ╚═╝   ╚═╝  ╚═╝╚══════╝");
        log.info(" ");
        log.info("Plugin: " + name + " v" + version + " | API " + (apiVersion != null ? apiVersion : "desconhecida"));
        log.info("Autores: " + authors);
        log.info("Servidor: " + serverVersion + " | Bukkit: " + bukkitVersion);
        log.info("Debug: " + (debug ? "ativo" : "desativado"));
        log.info("Inicialização: " + elapsedMs + " ms");
        log.info("Site: https://www.servidormagnatas.com.br");
        log.info(" ");
    }

}