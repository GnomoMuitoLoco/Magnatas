package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.Instant;
import java.util.*;
import java.util.logging.Level;

public class TituloConfigLoader {

    private final Magnatas plugin;
    private final File configFile;

    // Cache em memória
    private Map<String, Titulo> cacheTitulos;
    private long ultimaModificacaoArquivo;

    public TituloConfigLoader(Magnatas plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "Sistemas/Titulos/titulos.yml");
        this.cacheTitulos = new HashMap<>();
        this.ultimaModificacaoArquivo = 0L;
    }

    /**
     * Retorna todos os títulos, carregando do cache se possível.
     */
    public Collection<Titulo> getTitulos() {
        if (precisaRecarregar()) {
            carregarTitulos();
        }
        return Collections.unmodifiableCollection(cacheTitulos.values());
    }

    /**
     * Retorna um título específico pelo nome.
     */
    public Optional<Titulo> getTitulo(String nome) {
        if (precisaRecarregar()) {
            carregarTitulos();
        }
        return Optional.ofNullable(cacheTitulos.get(nome.toLowerCase()));
    }

    /**
     * Força recarregamento manual do arquivo.
     */
    public void reload() {
        carregarTitulos();
    }

    // ---------------------------
    // Internos
    // ---------------------------

    private boolean precisaRecarregar() {
        return cacheTitulos.isEmpty() || configFile.lastModified() > ultimaModificacaoArquivo;
    }

    private void carregarTitulos() {
        if (!configFile.exists()) {
            plugin.getLogger().warning("Arquivo de títulos não encontrado: " + configFile.getPath());
            return;
        }

        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection sec = config.getConfigurationSection("titulos");
            if (sec == null) {
                plugin.getLogger().warning("Seção 'titulos' ausente em " + configFile.getName());
                return;
            }

            Map<String, Titulo> novosTitulos = new HashMap<>();

            for (String key : sec.getKeys(false)) {
                try {
                    ConfigurationSection tSec = sec.getConfigurationSection(key);
                    if (tSec == null) continue;

                    String nomeVisivel = tSec.getString("nome", key);
                    String descricao = tSec.getString("descricao", "");
                    String permissao = tSec.getString("permissao", "magnatas.titulo." + key);
                    String obtencao = tSec.getString("obtencao", "Desconhecido");
                    boolean permanente = tSec.getBoolean("permanente", true);
                    long duracao = tSec.getLong("duracao", -1);
                    boolean loja = tSec.getBoolean("loja", false);
                    int preco = tSec.getInt("preco", 0);

                    // Expiração opcional
                    Instant expiraEm = null;
                    if (tSec.contains("expira_em")) {
                        long millis = tSec.getLong("expira_em");
                        if (millis > 0) expiraEm = Instant.ofEpochMilli(millis);
                    }

                    Titulo titulo = new Titulo(
                            key,
                            nomeVisivel,
                            descricao,
                            permissao,
                            obtencao,
                            permanente,
                            duracao,
                            loja,
                            preco,
                            expiraEm
                    );

                    novosTitulos.put(key.toLowerCase(), titulo);

                } catch (Exception e) {
                    plugin.getLogger().log(Level.WARNING,
                            "Erro ao carregar título '" + key + "' em " + configFile.getName(), e);
                }
            }

            // Atualiza cache
            this.cacheTitulos = novosTitulos;
            this.ultimaModificacaoArquivo = configFile.lastModified();

            plugin.getLogger().info("Carregados " + novosTitulos.size() + " títulos de " + configFile.getName());

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Falha ao carregar títulos de " + configFile.getName(), e);
        }
    }

    /**
     * Salva alterações no arquivo (se necessário).
     */
    public void salvar() {
        try {
            FileConfiguration config = YamlConfiguration.loadConfiguration(configFile);
            ConfigurationSection sec = config.createSection("titulos");

            for (Titulo titulo : cacheTitulos.values()) {
                ConfigurationSection tSec = sec.createSection(titulo.getNome());
                tSec.set("nome", titulo.getNomeVisivel());
                tSec.set("descricao", titulo.getDescricao());
                tSec.set("permissao", titulo.getPermissao());
                tSec.set("obtencao", titulo.getObtencao());
                tSec.set("permanente", titulo.isPermanente());
                tSec.set("duracao", titulo.getDuracaoMillis());
                tSec.set("loja", titulo.isLoja());
                tSec.set("preco", titulo.getPreco());
                titulo.getExpiraEm().ifPresent(exp -> tSec.set("expira_em", exp.toEpochMilli()));
            }

            config.save(configFile);
            this.ultimaModificacaoArquivo = configFile.lastModified();

        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Erro ao salvar títulos em " + configFile.getName(), e);
        }
    }
}