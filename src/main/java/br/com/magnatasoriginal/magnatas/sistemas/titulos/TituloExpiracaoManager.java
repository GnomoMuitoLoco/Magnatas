package br.com.magnatasoriginal.magnatas.sistemas.titulos;

import br.com.magnatasoriginal.magnatas.Magnatas;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class TituloExpiracaoManager {

    private final Magnatas plugin;
    private final File arquivo;
    private final YamlConfiguration config;
    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public TituloExpiracaoManager(Magnatas plugin) {
        this.plugin = plugin;
        this.arquivo = new File(plugin.getDataFolder(), "titulos_ativos.yml");
        this.config = YamlConfiguration.loadConfiguration(arquivo);
    }

    public LocalDateTime getExpiracao(UUID uuid, String titulo) {
        String path = "titulos_ativos." + titulo + "." + uuid.toString() + ".expires-on";
        if (!config.contains(path)) return null;

        String dataStr = config.getString(path);
        try {
            return LocalDateTime.parse(dataStr, formatter);
        } catch (Exception e) {
            return null;
        }
    }

    public void setExpiracao(UUID uuid, String titulo, LocalDateTime novaData) {
        String base = "titulos_ativos." + titulo + "." + uuid.toString();
        config.set(base + ".nick", plugin.getServer().getOfflinePlayer(uuid).getName());
        config.set(base + ".expires-on", novaData.format(formatter));
        config.set(base + ".ativo", true);
        salvar();
    }

    public void adicionarDuracao(UUID uuid, String titulo, long duracaoMillis) {
        LocalDateTime atual = getExpiracao(uuid, titulo);
        LocalDateTime nova;

        if (atual != null && atual.isAfter(LocalDateTime.now())) {
            nova = atual.plusNanos(duracaoMillis * 1_000_000); // converte millis para nanos
        } else {
            nova = LocalDateTime.now().plusNanos(duracaoMillis * 1_000_000);
        }

        setExpiracao(uuid, titulo, nova);
    }

    public boolean isTituloValido(UUID uuid, String titulo) {
        LocalDateTime expira = getExpiracao(uuid, titulo);
        return expira == null || LocalDateTime.now().isBefore(expira);
    }

    public void removerExpirado(UUID uuid, String titulo) {
        String path = "titulos_ativos." + titulo + "." + uuid.toString();
        config.set(path + ".ativo", false);
        salvar();
    }

    private void salvar() {
        try {
            config.save(arquivo);
        } catch (IOException e) {
            plugin.getLogger().warning("Não foi possível salvar titulos_ativos.yml: " + e.getMessage());
        }
    }
}