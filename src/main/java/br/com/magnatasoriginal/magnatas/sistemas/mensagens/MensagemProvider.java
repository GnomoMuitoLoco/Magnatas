package br.com.magnatasoriginal.magnatas.sistemas.mensagens;

import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;

public class MensagemProvider {

    private final File arquivo;
    private YamlConfiguration mensagens;
    private final String fallback;

    public MensagemProvider(File arquivo, String fallback) {
        this.arquivo = arquivo;
        this.fallback = fallback;
        this.mensagens = YamlConfiguration.loadConfiguration(arquivo);
    }

    public FileConfiguration getConfig() {
        return mensagens;
    }

    /**
     * Retorna a mensagem traduzida com placeholders posicionais (%1, %2...).
     */
    public String get(String key, String... args) {
        String message = mensagens.getString(key, fallback + ": " + key);
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (int i = 0; i < args.length; i++) {
            message = message.replace("%" + (i + 1), args[i]);
        }
        return message;
    }

    /**
     * Retorna a mensagem traduzida com placeholders nomeados (%player%, %titulo%, etc.).
     */
    public String getComPlaceholders(String key, Map<String, String> placeholders) {
        String message = mensagens.getString(key, fallback + ": " + key);
        message = ChatColor.translateAlternateColorCodes('&', message);
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            message = message.replace("%" + entry.getKey() + "%", entry.getValue());
        }
        return message;
    }

    /**
     * Envia a mensagem diretamente para um CommandSender.
     */
    public void send(CommandSender sender, String key, String... args) {
        sender.sendMessage(get(key, args));
    }

    /**
     * Envia a mensagem com placeholders nomeados.
     */
    public void send(CommandSender sender, String key, Map<String, String> placeholders) {
        sender.sendMessage(getComPlaceholders(key, placeholders));
    }

    /**
     * Recarrega o arquivo de mensagens sem reiniciar o servidor.
     */
    public void reload() {
        this.mensagens = YamlConfiguration.loadConfiguration(arquivo);
    }

    /**
     * Salva alterações no arquivo de mensagens.
     */
    public void save() {
        try {
            mensagens.save(arquivo);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}