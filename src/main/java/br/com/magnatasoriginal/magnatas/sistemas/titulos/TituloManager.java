package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.db.SQLiteManager;
import br.com.magnatasoriginal.magnatas.sistemas.titulos.util.DuracaoParser;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.sql.*;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;

public class TituloManager {

    private final Magnatas plugin;
    private final SQLiteManager db;

    // Cache em memória
    private final Map<String, Titulo> titulosRegistrados = new ConcurrentHashMap<>();
    private final Map<UUID, Set<String>> titulosPorJogador = new ConcurrentHashMap<>();
    private final Map<UUID, String> tituloEquipado = new ConcurrentHashMap<>();
    private final Map<String, Titulo> titulos = new HashMap<>();


    public TituloManager(Magnatas plugin, SQLiteManager db) {
        this.plugin = plugin;
        this.db = db;
        criarTabelas();
        carregarTitulos();
        carregarTitulosJogadores();
        carregarTitulosEquipados();
        iniciarVerificadorExpiracao();
    }
    // ---------------------------
    // Arquivo de configuração titulos.yml
    // ---------------------------
    public void carregarTitulos(File file) {
        YamlConfiguration config = YamlConfiguration.loadConfiguration(file);

        ConfigurationSection section = config.getConfigurationSection("titulos");
        if (section == null) return;

        for (String key : section.getKeys(false)) {
            ConfigurationSection tSec = section.getConfigurationSection(key);
            if (tSec == null) continue;

            String nomeVisivel = tSec.getString("nome_visivel", key);
            String descricao = tSec.getString("descricao", "");
            String obtencao = tSec.getString("obtencao", "");
            String permissao = tSec.getString("permissao", "");

            // Loja
            boolean loja = tSec.getBoolean("loja.disponivel", false);
            int preco = tSec.getInt("loja.preco", 0);

            // Duração
            String duracaoStr = tSec.getString("duracao", "permanente");
            long duracaoMillis = DuracaoParser.parse(duracaoStr);
            boolean permanente = duracaoMillis < 0;

            // Expiração (se não for permanente, calcula a partir de agora)
            Instant expiraEm = null;
            if (!permanente && duracaoMillis > 0) {
                expiraEm = Instant.now().plusMillis(duracaoMillis);
            }

            // Cria objeto Titulo usando o construtor que você mostrou
            Titulo titulo = new Titulo(
                    key,            // nome interno/id
                    nomeVisivel,
                    descricao,
                    permissao,
                    obtencao,
                    permanente,
                    duracaoMillis,
                    loja,
                    preco,
                    expiraEm
            );

            titulos.put(key.toLowerCase(), titulo);
        }
    }



    // ---------------------------
    // Registro de títulos
    // ---------------------------
    public void registrarTitulo(Titulo titulo) {
        titulosRegistrados.put(titulo.getNome(), titulo);
        salvarTitulo(titulo);
    }

    public Optional<Titulo> getTituloPorNome(String nome) {
        return Optional.ofNullable(titulosRegistrados.get(nome.toLowerCase()));
    }

    public Collection<Titulo> getTodosTitulos() {
        return Collections.unmodifiableCollection(titulosRegistrados.values());
    }

    // ---------------------------
    // Jogadores
    // ---------------------------
    public Set<String> getTitulosDoJogador(UUID uuid) {
        return Collections.unmodifiableSet(
                titulosPorJogador.getOrDefault(uuid, Collections.emptySet())
        );
    }

    public Set<UUID> getJogadoresComTitulos() {
        return Collections.unmodifiableSet(titulosPorJogador.keySet());
    }

    public void darTitulo(UUID uuid, String tituloNome) {
        final String nomeFinal = tituloNome.toLowerCase();
        titulosPorJogador.computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet()).add(nomeFinal);
        salvarTituloDoJogador(uuid, nomeFinal);
    }

    public void removerTitulo(UUID uuid, String tituloNome) {
        final String nomeFinal = tituloNome.toLowerCase();
        titulosPorJogador.getOrDefault(uuid, Collections.emptySet()).remove(nomeFinal);
        removerTituloDoJogador(uuid, nomeFinal);
    }

    public void equiparTitulo(Player player, String tituloNome) {
        final UUID uuid = player.getUniqueId();
        final String nomeFinal = tituloNome.toLowerCase();

        if (!getTitulosDoJogador(uuid).contains(nomeFinal)) return;

        getTituloPorNome(nomeFinal).ifPresent(titulo -> {
            if (!titulo.isExpirado()) {
                tituloEquipado.put(uuid, nomeFinal);
                salvarTituloEquipado(uuid, nomeFinal);
            }
        });
    }

    public void removerTituloEquipado(UUID uuid) {
        tituloEquipado.remove(uuid);
        removerTituloEquipadoDB(uuid);
    }

    public Optional<String> getTituloEquipado(UUID uuid) {
        return Optional.ofNullable(tituloEquipado.get(uuid));
    }

    // ---------------------------
    // Expiração
    // ---------------------------
    private void iniciarVerificadorExpiracao() {
        Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            for (UUID uuid : new HashSet<>(titulosPorJogador.keySet())) {
                titulosPorJogador.get(uuid).removeIf(nome -> {
                    Optional<Titulo> t = getTituloPorNome(nome);
                    return t.map(Titulo::isExpirado).orElse(false);
                });
            }
        }, 20L * 60, 20L * 60); // a cada 1 minuto
    }

    public void verificarExpiracoes(UUID uuid) {
        Set<String> titulos = getTitulosDoJogador(uuid);

        for (String nome : new HashSet<>(titulos)) {
            Optional<Titulo> opt = getTituloPorNome(nome);
            if (opt.isPresent()) {
                Titulo titulo = opt.get();
                if (titulo.isExpirado()) {
                    removerTitulo(uuid, nome);

                    // Se o jogador tinha esse título equipado, remove também
                    getTituloEquipado(uuid).ifPresent(equipado -> {
                        if (equipado.equalsIgnoreCase(nome)) {
                            removerTituloEquipado(uuid);
                        }
                    });
                }
            }
        }
    }

    // ---------------------------
    // Persistência
    // ---------------------------
    private void criarTabelas() {
        try (Connection conn = db.openConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS titulos (" +
                    "nome TEXT PRIMARY KEY, " +
                    "descricao TEXT, " +
                    "expira_em INTEGER)");
            stmt.execute("CREATE TABLE IF NOT EXISTS jogador_titulos (" +
                    "uuid TEXT, titulo_nome TEXT, PRIMARY KEY(uuid, titulo_nome))");
            stmt.execute("CREATE TABLE IF NOT EXISTS jogador_titulo_equipado (" +
                    "uuid TEXT PRIMARY KEY, titulo_nome TEXT)");
        } catch (SQLException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao criar tabelas de títulos", e);
        }
    }

    private void salvarTitulo(Titulo titulo) {
        try (Connection conn = db.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO titulos (nome, descricao, expira_em) VALUES (?, ?, ?)")) {
            ps.setString(1, titulo.getNome());
            ps.setString(2, titulo.getDescricao());
            ps.setObject(3, titulo.getExpiraEm().map(Instant::toEpochMilli).orElse(null));
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao salvar título " + titulo.getNome(), e);
        }
    }

    private void carregarTitulos() {
        try (Connection conn = db.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT * FROM titulos")) {
            while (rs.next()) {
                String nome = rs.getString("nome");
                String descricao = rs.getString("descricao");
                Long expiraMillis = rs.getLong("expira_em");
                Instant expira = rs.wasNull() ? null : Instant.ofEpochMilli(expiraMillis);

                Titulo titulo = new Titulo(
                        nome,
                        nome,
                        descricao,
                        "magnatas.titulos." + nome,
                        "Manual",
                        expira == null,
                        -1,
                        false,
                        0,
                        expira
                );
                titulosRegistrados.put(nome, titulo);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao carregar títulos do banco", e);
        }
    }

    private void carregarTitulosJogadores() {
        try (Connection conn = db.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, titulo_nome FROM jogador_titulos")) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String tituloNome = rs.getString("titulo_nome");
                titulosPorJogador
                        .computeIfAbsent(uuid, k -> ConcurrentHashMap.newKeySet())
                        .add(tituloNome);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao carregar títulos de jogadores", e);
        }
    }

    private void carregarTitulosEquipados() {
        try (Connection conn = db.openConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT uuid, titulo_nome FROM jogador_titulo_equipado")) {
            while (rs.next()) {
                UUID uuid = UUID.fromString(rs.getString("uuid"));
                String tituloNome = rs.getString("titulo_nome");
                tituloEquipado.put(uuid, tituloNome);
            }
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao carregar títulos equipados", e);
        }
    }

    private void salvarTituloDoJogador(UUID uuid, String tituloNome) {
        try (Connection conn = db.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR IGNORE INTO jogador_titulos (uuid, titulo_nome) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, tituloNome);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao salvar título do jogador", e);
        }
    }

    private void removerTituloDoJogador(UUID uuid, String tituloNome) {
        try (Connection conn = db.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM jogador_titulos WHERE uuid = ? AND titulo_nome = ?")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, tituloNome);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao remover título do jogador", e);
        }
    }

    private void salvarTituloEquipado(UUID uuid, String tituloNome) {
        try (Connection conn = db.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "INSERT OR REPLACE INTO jogador_titulo_equipado (uuid, titulo_nome) VALUES (?, ?)")) {
            ps.setString(1, uuid.toString());
            ps.setString(2, tituloNome);
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao salvar título equipado", e);
        }
    }

    private void removerTituloEquipadoDB(UUID uuid) {
        try (Connection conn = db.openConnection();
             PreparedStatement ps = conn.prepareStatement(
                     "DELETE FROM jogador_titulo_equipado WHERE uuid = ?")) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            plugin.getLogger().log(Level.WARNING, "Erro ao remover título equipado", e);
        }
    }
}