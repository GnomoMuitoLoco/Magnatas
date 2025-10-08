package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import br.com.magnatasoriginal.magnatas.db.SQLiteManager;
import org.bukkit.entity.Player;

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
                String expiraStr = rs.getString("expiraEm");
                LocalDateTime expiraEm = expiraStr != null ? LocalDateTime.parse(expiraStr) : null;

                Titulo titulo = new Titulo(nome, plugin.colorir(nome), plugin.colorir(descricao),
                        "magnatas.titulos." + nome.toLowerCase(), nome.toLowerCase(), "Banco",
                        expiraEm != null ? java.time.Duration.between(LocalDateTime.now(), expiraEm).toMillis() : 0);

                titulosRegistrados.put(nome.toLowerCase(), titulo);
            }

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void carregarTitulosConfigurados(File configFile) {
        if (!configFile.exists()) return;

        TituloConfigLoader loader = new TituloConfigLoader(configFile);
        for (Titulo titulo : loader.getTitulos()) {
            registrarTitulo(titulo);
        }
    }

    public void darTitulo(Player player, String nome) {
        titulosPorJogador.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>()).add(nome.toLowerCase());
        salvarTituloDoJogador(player.getUniqueId(), nome.toLowerCase());
    }

    public Set<String> getTitulosDoJogador(Player player) {
        Set<String> titulos = new HashSet<>();

        // Títulos salvos no banco
        Set<String> conquistados = titulosPorJogador.getOrDefault(player.getUniqueId(), new HashSet<>());
        titulos.addAll(conquistados);

        // Títulos por permissão
        for (Titulo titulo : titulosRegistrados.values()) {
            String perm = titulo.getPermissao();

            // Se não exige permissão, é público
            if (perm == null || perm.isEmpty()) {
                titulos.add(titulo.getNome());
                continue;
            }

            // Se jogador tem permissão, adiciona
            if (player.hasPermission(perm)) {
                titulos.add(titulo.getNome());
            }

            // Se é OP ou tem '*', vê tudo
            if (player.isOp() || player.hasPermission("*")) {
                titulos.add(titulo.getNome());
            }
        }

        return titulos;
    }

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

    public Map<UUID, Set<String>> getTitulosPorJogador() {
        return Collections.unmodifiableMap(titulosPorJogador);
    }

    public int getQuantidadeTitulos(Player player) {
        return getTitulosDoJogador(player).size();
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

    public void recarregarTitulos() {
        titulosRegistrados.clear(); // limpa os títulos atuais
        carregarTitulos();          // recarrega do arquivo titulos.yml
    }
}