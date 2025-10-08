package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.db.SQLiteManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.*;

public class TituloManager {

    private final Magnatas plugin;
    private final SQLiteManager sqlite;

    private final Map<String, Titulo> titulosRegistrados = new HashMap<>();
    private final Map<UUID, Set<String>> titulosPorJogador = new HashMap<>();
    private final Map<UUID, String> tituloEquipado = new HashMap<>();

    public TituloManager(Magnatas plugin, SQLiteManager sqlite) {
        this.plugin = plugin;
        this.sqlite = sqlite;

        criarTabelas();
        carregarTitulos();
        carregarTitulosConfigurados(new File(plugin.getDataFolder(), "Sistemas/Titulos/titulos.yml"));
        carregarTitulosJogadores();
        carregarTitulosEquipados();

        // Carrega títulos ativos do YAML
        carregarTitulosAtivos();
        // Inicia verificação periódica de expiração
        iniciarVerificadorExpiracao();
    }

    private void criarTabelas() {
        try (Connection conn = sqlite.openConnection();
             Statement stmt = conn.createStatement()) {

            stmt.execute("CREATE TABLE IF NOT EXISTS titulos (" +
                    "nome TEXT PRIMARY KEY, " +
                    "descricao TEXT, " +
                    "expiraEm TEXT)");

            stmt.execute("CREATE TABLE IF NOT EXISTS jogador_titulos (" +
                    "uuid TEXT, " +
                    "titulo_nome TEXT, " +
                    "PRIMARY KEY (uuid, titulo_nome), " +
                    "FOREIGN KEY (titulo_nome) REFERENCES titulos(nome))");

            stmt.execute("CREATE TABLE IF NOT EXISTS jogador_titulo_equipado (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "titulo_nome TEXT, " +
                    "FOREIGN KEY (titulo_nome) REFERENCES titulos(nome))");

        } catch (SQLException e) {
            plugin.getLogger().warning("Erro ao criar tabelas de títulos:");
            e.printStackTrace();
        }
    }

    // ---------------- Registro e carregamento ----------------

    public void registrarTitulo(Titulo titulo) {
        titulosRegistrados.put(titulo.getNome().toLowerCase(), titulo);
        salvarTitulo(titulo);
    }

    public Titulo getTituloPorNome(String nome) {
        return titulosRegistrados.get(nome.toLowerCase());
    }

    public Collection<Titulo> getTodosTitulos() {
        return titulosRegistrados.values();
    }

    private void salvarTitulo(Titulo titulo) {
        try (Connection conn = sqlite.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO titulos (nome, descricao, expiraEm) VALUES (?, ?, ?)")) {
            ps.setString(1, titulo.getNome());
            ps.setString(2, titulo.getDescricao());
            ps.setString(3, titulo.getExpiraEm() != null ? titulo.getExpiraEm().toString() : null);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void apagarTitulo(String nome) {
        titulosRegistrados.remove(nome.toLowerCase());
        try (Connection conn = sqlite.openConnection();
             PreparedStatement ps = conn.prepareStatement("DELETE FROM titulos WHERE nome = ?")) {
            ps.setString(1, nome);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarTitulos() {
        try (Connection conn = sqlite.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM titulos")) {

            while (rs.next()) {
                String nome = rs.getString("nome");
                String descricao = rs.getString("descricao");

                // Se o banco tiver uma coluna de expiração, use-a
                Timestamp expira = null;
                try {
                    expira = rs.getTimestamp("expira_em");
                } catch (SQLException ignored) {
                    // coluna pode não existir, então ignoramos
                }

                Titulo titulo;
                if (expira != null) {
                    // Usa construtor legado com expiração exata
                    titulo = new Titulo(
                            nome,
                            plugin.colorir(descricao),
                            expira.toLocalDateTime()
                    );
                } else {
                    // Usa construtor principal com todos os campos
                    titulo = new Titulo(
                            nome,
                            plugin.colorir("&f" + nome),                 // nome visível
                            plugin.colorir(descricao),                   // descrição
                            "magnatas.titulos." + nome.toLowerCase(),    // permissão
                            "Banco",                                     // obtenção
                            "permanente",                                // duração padrão
                            false,                                       // loja
                            0,                                           // preço
                            nome                                         // nomePermissao
                    );
                }

                titulosRegistrados.put(nome.toLowerCase(), titulo);
            }
        } catch (SQLException e) {
            plugin.getLogger().warning("Erro ao carregar títulos do banco: " + e.getMessage());
        }
    }

    public void carregarTitulosConfigurados(File configFile) {
        if (!configFile.exists()) return;

        TituloConfigLoader loader = new TituloConfigLoader(configFile);
        for (Titulo titulo : loader.getTitulos()) {
            registrarTitulo(titulo);
        }
    }
    // ---------------- Verificação de títulos conquistados ----------------

    public boolean possuiTituloConquistado(UUID uuid, String nome) {
        return titulosPorJogador
                .getOrDefault(uuid, Collections.emptySet())
                .contains(nome.toLowerCase());
    }

    // ---------------- Dar/remover títulos ----------------

    public void darTitulo(Player player, String nome) {
        darTitulo(player.getUniqueId(), nome);
    }

    public void darTitulo(UUID uuid, String nome) {
        titulosPorJogador.computeIfAbsent(uuid, k -> new HashSet<>()).add(nome.toLowerCase());
        salvarTituloDoJogador(uuid, nome.toLowerCase());
    }

    public void removerTitulo(UUID uuid, String nome) {
        Set<String> titulos = titulosPorJogador.get(uuid);
        if (titulos != null) {
            titulos.remove(nome.toLowerCase());
        }
        removerTituloDoJogador(uuid, nome.toLowerCase());
    }

    // ---------------- Consultas ----------------

    public Set<String> getTitulosDoJogador(Player player) {
        return getTitulosDoJogador(player.getUniqueId(), player);
    }

    public Set<String> getTitulosDoJogador(UUID uuid) {
        return getTitulosDoJogador(uuid, null);
    }

    private Set<String> getTitulosDoJogador(UUID uuid, Player player) {
        Set<String> titulos = new HashSet<>();

        // Banco
        Set<String> conquistados = titulosPorJogador.getOrDefault(uuid, new HashSet<>());
        titulos.addAll(conquistados);

        // Permissões
        if (player != null) {
            for (Titulo titulo : titulosRegistrados.values()) {
                String perm = titulo.getPermissao();
                if (perm == null || perm.isEmpty() || player.hasPermission(perm) || player.isOp() || player.hasPermission("*")) {
                    titulos.add(titulo.getNome().toLowerCase());
                }
            }
        }

        return titulos;
    }

    // ---------------- Equipar/remover ----------------

    public void equiparTitulo(Player player, String nome) {
        if (getTitulosDoJogador(player).contains(nome.toLowerCase())) {
            tituloEquipado.put(player.getUniqueId(), nome.toLowerCase());
            salvarTituloEquipado(player.getUniqueId(), nome.toLowerCase());
        }
    }

    public void removerTitulo(Player player) {
        tituloEquipado.remove(player.getUniqueId());
        removerTituloEquipado(player.getUniqueId());
    }

    public String getTituloEquipado(Player player) {
        return tituloEquipado.get(player.getUniqueId());
    }

    // ---------------- Persistência banco ----------------

    private void salvarTituloDoJogador(UUID uuid, String tituloNome) {
        try (Connection conn = sqlite.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR IGNORE INTO jogador_titulos (uuid, titulo_nome) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, tituloNome);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removerTituloDoJogador(UUID uuid, String tituloNome) {
        try (Connection conn = sqlite.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM jogador_titulos WHERE uuid = ? AND titulo_nome = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, tituloNome);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void salvarTituloEquipado(UUID uuid, String tituloNome) {
        try (Connection conn = sqlite.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO jogador_titulo_equipado (uuid, titulo_nome) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, tituloNome);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void removerTituloEquipado(UUID uuid) {
        try (Connection conn = sqlite.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM jogador_titulo_equipado WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarTitulosJogadores() {
        try (Connection conn = sqlite.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM jogador_titulos")) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String tituloNome = rs.getString("titulo_nome");
                titulosPorJogador.computeIfAbsent(uuid, k -> new HashSet<>()).add(tituloNome.toLowerCase());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void carregarTitulosEquipados() {
        try (Connection conn = sqlite.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM jogador_titulo_equipado")) {

            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String tituloNome = rs.getString("titulo_nome");
                tituloEquipado.put(uuid, tituloNome.toLowerCase());
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<UUID, Set<String>> getTitulosPorJogador() {
        return Collections.unmodifiableMap(titulosPorJogador);
    }

    public int getQuantidadeTitulos(Player player) {
        return getTitulosDoJogador(player).size();
    }

    public void recarregarTitulos() {
        titulosRegistrados.clear();
        carregarTitulos();
        carregarTitulosConfigurados(new File(plugin.getDataFolder(), "Sistemas/Titulos/titulos.yml"));
        carregarTitulosAtivos();
    }

    // ---------------- Integração com titulos_ativos.yml ----------------

    public void carregarTitulosAtivos() {
        FileConfiguration config = plugin.getTitulosAtivosConfig();
        ConfigurationSection section = config.getConfigurationSection("activeTitles");
        if (section == null) return;

        for (String tituloNome : section.getKeys(false)) {
            ConfigurationSection tituloSec = section.getConfigurationSection(tituloNome);
            if (tituloSec == null) continue;

            for (String uuidStr : tituloSec.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                boolean active = tituloSec.getBoolean(uuidStr + ".active", false);
                long duration = tituloSec.getLong(uuidStr + ".duration", -1);

                if (active && (duration == -1 || System.currentTimeMillis() < duration)) {
                    // ainda válido → adiciona ao jogador
                    titulosPorJogador.computeIfAbsent(uuid, k -> new HashSet<>()).add(tituloNome.toLowerCase());
                } else if (active) {
                    // expirado → marca como inativo
                    tituloSec.set(uuidStr + ".active", false);
                }
            }
        }
        plugin.salvarTitulosAtivos();
    }

    private void iniciarVerificadorExpiracao() {
        new BukkitRunnable() {
            @Override
            public void run() {
                FileConfiguration config = plugin.getTitulosAtivosConfig();
                ConfigurationSection section = config.getConfigurationSection("activeTitles");
                if (section == null) return;

                boolean changed = false;

                for (String tituloNome : section.getKeys(false)) {
                    ConfigurationSection tituloSec = section.getConfigurationSection(tituloNome);
                    if (tituloSec == null) continue;

                    for (String uuidStr : tituloSec.getKeys(false)) {
                        long duration = tituloSec.getLong(uuidStr + ".duration", -1);
                        boolean active = tituloSec.getBoolean(uuidStr + ".active", false);

                        if (active && duration > 0 && System.currentTimeMillis() > duration) {
                            tituloSec.set(uuidStr + ".active", false);
                            changed = true;

                            Player p = Bukkit.getPlayer(UUID.fromString(uuidStr));
                            if (p != null) {
                                p.sendMessage(ChatColor.RED + "Seu título " + tituloNome + " expirou!");
                            }
                        }
                    }
                }

                if (changed) plugin.salvarTitulosAtivos();
            }
        }.runTaskTimer(plugin, 20L, 1200L); // roda a cada 60s
    }
}